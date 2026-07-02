package com.example.milkiwai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.milkiwai.data.database.AppDatabase
import com.example.milkiwai.data.entity.Delivery
import com.example.milkiwai.repository.DeliveryRepository
import kotlinx.coroutines.launch

import androidx.lifecycle.MutableLiveData
import com.example.milkiwai.data.entity.Customer
import com.example.milkiwai.data.entity.SubscriptionWithBrand
import com.example.milkiwai.data.entity.QuantityChange
import com.example.milkiwai.data.entity.Vacation
import com.example.milkiwai.data.model.DeliveryItem
import com.example.milkiwai.utils.BillingCalculator
import java.text.SimpleDateFormat
import java.util.*

class DeliveryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DeliveryRepository
    private val db = AppDatabase.getDatabase(application)
    private val calculator = BillingCalculator()
    
    private val _deliveryItems = MutableLiveData<List<DeliveryItem>>()
    val deliveryItems: LiveData<List<DeliveryItem>> = _deliveryItems

    init {
        repository = DeliveryRepository(db.deliveryDao())
    }

    fun loadDeliveries(date: Date) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
            
            val customers = db.customerDao().getAllCustomersList()
            val subscriptions = db.subscriptionDao().getAllSubscriptionsWithBrandList()
            val changes = db.quantityChangeDao().getAllChangesList()
            val vacations = db.vacationDao().getAllVacationsList()
            
            val schedule = calculator.calculateDailySchedule(date, customers, subscriptions, changes, vacations)
            
            // Fetch existing delivery records from DB
            val existingDeliveries = db.deliveryDao().getDeliveriesByStatus(dateStr, "DELIVERED") + 
                                     db.deliveryDao().getDeliveriesByStatus(dateStr, "MISSED") +
                                     db.deliveryDao().getDeliveriesByStatus(dateStr, "CANCELLED")
            
            val items = schedule.map { scheduledItem ->
                val delivery = existingDeliveries.find { 
                    it.customerId == scheduledItem.customer.id && it.brandId == scheduledItem.brand.id 
                }
                scheduledItem.copy(delivery = delivery)
            }
            
            _deliveryItems.value = items
        }
    }

    fun markAsDelivered(item: DeliveryItem, date: Date) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
            val delivery = Delivery(
                customerId = item.customer.id,
                brandId = item.brand.id,
                quantity = item.scheduledQuantity,
                deliveryDate = dateStr,
                status = "DELIVERED"
            )
            repository.insert(delivery)
            loadDeliveries(date) // Reload
        }
    }

    fun markAsMissed(item: DeliveryItem, date: Date, remarks: String) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
            val delivery = Delivery(
                customerId = item.customer.id,
                brandId = item.brand.id,
                quantity = item.scheduledQuantity,
                deliveryDate = dateStr,
                status = "MISSED",
                remarks = remarks
            )
            repository.insert(delivery)
            loadDeliveries(date) // Reload
        }
    }

    fun insert(delivery: Delivery) = viewModelScope.launch {
        repository.insert(delivery)
    }

    fun update(delivery: Delivery) = viewModelScope.launch {
        repository.update(delivery)
    }

    fun delete(delivery: Delivery) = viewModelScope.launch {
        repository.delete(delivery)
    }

    fun getDeliveriesByDate(date: String): LiveData<List<Delivery>> = repository.getDeliveriesByDate(date)

    fun getDeliveriesByCustomer(customerId: Int): LiveData<List<Delivery>> = repository.getDeliveriesByCustomer(customerId)
}
