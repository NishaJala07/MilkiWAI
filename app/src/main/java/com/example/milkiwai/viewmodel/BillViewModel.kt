package com.example.milkiwai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.milkiwai.data.database.AppDatabase
import com.example.milkiwai.data.entity.BillItem
import com.example.milkiwai.data.entity.MonthlyBill
import com.example.milkiwai.data.entity.Vacation
import com.example.milkiwai.repository.*
import com.example.milkiwai.utils.BillingCalculator
import com.example.milkiwai.utils.pdf.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.milkiwai.utils.NotificationHelper
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

class BillViewModel(application: Application) : AndroidViewModel(application) {

    private val billRepository: BillRepository
    private val customerRepository: CustomerRepository
    private val subRepository: SubscriptionRepository
    private val changeRepository: QuantityChangeRepository
    private val vacationRepository: VacationRepository
    private val deliveryRepository: DeliveryRepository
    private val firebaseRepo = FirebaseRepository()
    private val billingCalculator = BillingCalculator()
    private val pdfGenerator = PdfGenerator(application)
    private val notificationHelper = NotificationHelper(application)

    val allBills = AppDatabase.getDatabase(application).monthlyBillDao().getAllBillsWithCustomer()

    init {
        val db = AppDatabase.getDatabase(application)
        billRepository = BillRepository(db.monthlyBillDao(), db.billItemDao())
        customerRepository = CustomerRepository(db.customerDao())
        subRepository = SubscriptionRepository(db.subscriptionDao())
        changeRepository = QuantityChangeRepository(db.quantityChangeDao())
        vacationRepository = VacationRepository(db.vacationDao())
        deliveryRepository = DeliveryRepository(db.deliveryDao())
    }

    fun generateBills(month: Int, year: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            val customers = customerRepository.getAllCustomersList()
            val billDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

            for (customer in customers) {
                val subs = subRepository.getSubscriptionsByCustomerList(customer.id)
                val changes = changeRepository.getChangesByCustomerList(customer.id).map { it.quantityChange }
                val vacations = vacationRepository.getVacationsByCustomerList(customer.id)
                val deliveries = fetchDeliveriesForMonth(customer.id, month, year)

                val calculatedItems = billingCalculator.calculateMonthlyBill(month, year, subs, changes, vacations, deliveries)

                if (calculatedItems.isNotEmpty()) {
                    val totalAmount = calculatedItems.sumOf { it.amount }
                    val bill = MonthlyBill(
                        customerId = customer.id,
                        month = month + 1, // 1-indexed for display
                        year = year,
                        totalAmount = totalAmount,
                        billDate = billDate
                    )
                    val billId = billRepository.insertBill(bill).toInt()

                    val billItemsWithBrand = mutableListOf<com.example.milkiwai.data.entity.BillItemWithBrand>()
                    val billItemsForFirebase = mutableListOf<BillItem>()
                    for (item in calculatedItems) {
                        val billItem = BillItem(
                            billId = billId,
                            brandId = item.brandId,
                            totalQuantity = item.totalQuantity,
                            ratePerLitre = item.rate,
                            amount = item.amount
                        )
                        billRepository.insertItem(billItem)
                        billItemsForFirebase.add(billItem)
                        
                        // For PDF generation we need the brand object
                        val brand = subs.find { it.brand?.id == item.brandId }?.brand
                        if (brand != null) {
                            billItemsWithBrand.add(com.example.milkiwai.data.entity.BillItemWithBrand(billItem, brand))
                        }
                    }

                    // Generate PDF
                    val pdfPath = pdfGenerator.generateBillPdf(customer, bill.copy(id = billId), billItemsWithBrand)
                    val finalBill = bill.copy(id = billId, pdfPath = pdfPath)
                    billRepository.updateBill(finalBill)
                    
                    // Backup Bill
                    firebaseRepo.backupBills(listOf(finalBill), billItemsForFirebase)

                    // Step 12: Send Alert
                    notificationHelper.showNotification(
                        "Bill Generated",
                        "Bill for ${customer.name} for ${DateFormatSymbols().months[month]} $year has been generated: ₹$totalAmount"
                    )
                }
            }
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun markAsPaid(bill: MonthlyBill) {
        viewModelScope.launch {
            billRepository.updateBill(bill.copy(paymentStatus = "PAID"))
            // Backup bill item not strictly needed if we backup the bill doc again
            firebaseRepo.backupBills(listOf(bill.copy(paymentStatus = "PAID")), emptyList())
        }
    }

    fun updateBill(bill: MonthlyBill) {
        viewModelScope.launch {
            billRepository.updateBill(bill)
            firebaseRepo.backupBills(listOf(bill), emptyList())
        }
    }

    fun getItemsForBill(billId: Int) = billRepository.getItemsForBill(billId)

    fun getVacations(customerId: Int) = vacationRepository.getVacationsByCustomer(customerId)

    fun insertVacation(vacation: Vacation) {
        viewModelScope.launch { 
            vacationRepository.insert(vacation)
            firebaseRepo.backupVacations(listOf(vacation))
        }
    }

    fun updateVacation(vacation: Vacation) {
        viewModelScope.launch { 
            vacationRepository.update(vacation)
            firebaseRepo.backupVacations(listOf(vacation))
        }
    }

    fun deleteVacation(vacation: Vacation) {
        viewModelScope.launch { vacationRepository.delete(vacation) }
    }

    private suspend fun fetchDeliveriesForMonth(customerId: Int, month: Int, year: Int): List<com.example.milkiwai.data.entity.Delivery> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val db = AppDatabase.getDatabase(getApplication())
        
        val deliveries = mutableListOf<com.example.milkiwai.data.entity.Delivery>()
        for (day in 1..daysInMonth) {
            calendar.set(year, month, day)
            val dateStr = sdf.format(calendar.time)
            deliveries.addAll(db.deliveryDao().getDeliveryByCustomerAndDate(customerId, dateStr))
        }
        return deliveries
    }
}
