package com.example.milkiwai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.milkiwai.data.database.AppDatabase
import com.example.milkiwai.data.entity.QuantityChange
import com.example.milkiwai.data.entity.SubscriptionWithBrand
import com.example.milkiwai.data.entity.Vacation
import com.example.milkiwai.repository.*
import com.example.milkiwai.utils.BillingCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ReportRepository
    private val customerRepository: CustomerRepository
    private val subRepository: SubscriptionRepository
    private val changeRepository: QuantityChangeRepository
    private val vacationRepository: VacationRepository
    private val billingCalculator = BillingCalculator()

    val procurementResult = MutableStateFlow<List<BillingCalculator.ProcurementItem>>(emptyList())

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ReportRepository(db.monthlyBillDao(), db.billItemDao())
        customerRepository = CustomerRepository(db.customerDao())
        subRepository = SubscriptionRepository(db.subscriptionDao())
        changeRepository = QuantityChangeRepository(db.quantityChangeDao())
        vacationRepository = VacationRepository(db.vacationDao())
    }

    fun getTodayRevenue() : androidx.lifecycle.LiveData<Double?> {
        val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        return repository.getDailyRevenue(today)
    }

    fun getMonthlyRevenue(month: Int, year: Int) = repository.getMonthlyRevenue(month + 1, year)
    fun getPendingPayments() = repository.getPendingPayments()
    fun getCustomerWiseRevenue() = repository.getCustomerWiseRevenue()
    fun getBrandWiseSales() = repository.getBrandWiseSales()

    fun getAllCustomers() = customerRepository.allCustomers
    
    fun getAllBrands() = AppDatabase.getDatabase(getApplication()).milkBrandDao().getAllBrands()

    fun getDeliverySuccessRate(date: Date, onResult: (Double) -> Unit) {
        viewModelScope.launch {
            val db = AppDatabase.getDatabase(getApplication())
            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
            
            val customers = db.customerDao().getAllCustomersList()
            val subscriptions = db.subscriptionDao().getAllSubscriptionsWithBrandList()
            val changes = db.quantityChangeDao().getAllChangesList()
            val vacations = db.vacationDao().getAllVacationsList()
            
            val schedule = billingCalculator.calculateDailySchedule(date, customers, subscriptions, changes, vacations)
            val deliveries = db.deliveryDao().getDeliveriesByStatus(dateStr, "DELIVERED")
            
            val total = schedule.size
            val successCount = deliveries.size // This assumes 1 delivery per scheduled item
            // More accurately we should match customer/brand
            
            val rate = if (total > 0) (successCount.toDouble() / total * 100) else 0.0
            onResult(rate)
        }
    }

    fun calculateTomorrowProcurement() {
        viewModelScope.launch {
            val tomorrow = Calendar.getInstance()
            tomorrow.add(Calendar.DAY_OF_YEAR, 1)
            val date = tomorrow.time

            val customers = customerRepository.getAllCustomersList()
            val allSubs = mutableListOf<SubscriptionWithBrand>()
            val allChanges = mutableListOf<QuantityChange>()
            val allVacations = mutableListOf<Vacation>()

            for (customer in customers) {
                allSubs.addAll(subRepository.getSubscriptionsByCustomerList(customer.id))
                allChanges.addAll(changeRepository.getChangesByCustomerList(customer.id).map { it.quantityChange })
                allVacations.addAll(vacationRepository.getVacationsByCustomerList(customer.id))
            }

            val result = billingCalculator.calculateDailyProcurement(date, allSubs, allChanges, allVacations)
            procurementResult.value = result
        }
    }
}
