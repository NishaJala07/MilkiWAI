package com.example.milkiwai.utils

import com.example.milkiwai.data.entity.CustomerSubscription
import com.example.milkiwai.data.entity.QuantityChange
import com.example.milkiwai.data.entity.Vacation
import java.text.SimpleDateFormat
import java.util.*

class BillingCalculator {

    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun calculateMonthlyBill(
        month: Int, // 0-indexed (0=Jan, 11=Dec)
        year: Int,
        subscriptions: List<com.example.milkiwai.data.entity.SubscriptionWithBrand>,
        quantityChanges: List<QuantityChange>,
        vacations: List<Vacation>,
        deliveries: List<com.example.milkiwai.data.entity.Delivery> = emptyList()
    ): List<CalculatedItem> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val resultItems = mutableListOf<CalculatedItem>()

        for (subWithBrand in subscriptions) {
            val sub = subWithBrand.subscription
            if (!sub.isActive) continue
            
            val brand = subWithBrand.brand ?: continue
            var totalQty = 0.0

            for (day in 1..daysInMonth) {
                val currentDay = Calendar.getInstance()
                currentDay.set(year, month, day)
                val currentDateStr = dateFormatter.format(currentDay.time)
                
                // 1. Check Actual Delivery Record First
                val actualDelivery = deliveries.find { 
                    it.deliveryDate == currentDateStr && 
                    it.customerId == sub.customerId && 
                    it.brandId == brand.id 
                }
                
                if (actualDelivery != null) {
                    if (actualDelivery.status == "DELIVERED") {
                        totalQty += actualDelivery.quantity
                    }
                    continue // Skip subscription logic if delivery record exists
                }

                // 2. Fallback to Subscription/Vacation logic for dates without delivery records
                val currentDate = currentDay.time
                if (isDateInRange(currentDate, vacations)) {
                    continue
                }

                val change = findQuantityChange(currentDate, brand.id, quantityChanges)
                if (change != null) {
                    totalQty += change.newQuantity
                } else {
                    totalQty += sub.quantity
                }
            }

            if (totalQty > 0) {
                resultItems.add(
                    CalculatedItem(
                        brandId = brand.id,
                        brandName = brand.brandName,
                        totalQuantity = totalQty,
                        rate = brand.pricePerLitre,
                        amount = totalQty * brand.pricePerLitre
                    )
                )
            }
        }

        return resultItems
    }

    fun calculateDailySchedule(
        date: Date,
        allCustomers: List<com.example.milkiwai.data.entity.Customer>,
        allSubscriptions: List<com.example.milkiwai.data.entity.SubscriptionWithBrand>,
        allChanges: List<QuantityChange>,
        allVacations: List<Vacation>
    ): List<com.example.milkiwai.data.model.DeliveryItem> {
        val result = mutableListOf<com.example.milkiwai.data.model.DeliveryItem>()
        
        for (customer in allCustomers) {
            val customerSubs = allSubscriptions.filter { 
                it.subscription.customerId == customer.id && it.subscription.isActive 
            }
            val customerVacations = allVacations.filter { it.customerId == customer.id }
            
            if (isDateInRange(date, customerVacations)) {
                continue
            }
            
            for (subWithBrand in customerSubs) {
                val sub = subWithBrand.subscription
                val brand = subWithBrand.brand ?: continue
                
                val customerChanges = allChanges.filter { it.customerId == customer.id && it.brandId == brand.id }
                val change = findQuantityChange(date, brand.id, customerChanges)
                
                val qty = change?.newQuantity ?: sub.quantity
                
                if (qty > 0) {
                    result.add(
                        com.example.milkiwai.data.model.DeliveryItem(
                            customer = customer,
                            brand = brand,
                            scheduledQuantity = qty
                        )
                    )
                }
            }
        }
        
        return result
    }

    private fun isDateInRange(date: Date, vacations: List<Vacation>): Boolean {
        for (v in vacations) {
            val start = dateFormatter.parse(v.startDate)
            val end = dateFormatter.parse(v.endDate)
            if (date.compareTo(start) >= 0 && date.compareTo(end) <= 0) {
                return true
            }
        }
        return false
    }

    private fun findQuantityChange(date: Date, brandId: Int, changes: List<QuantityChange>): QuantityChange? {
        for (c in changes) {
            if (c.brandId == brandId && c.status == "ACTIVE") {
                val start = dateFormatter.parse(c.startDate)
                val end = dateFormatter.parse(c.endDate)
                if (date.compareTo(start) >= 0 && date.compareTo(end) <= 0) {
                    return c
                }
            }
        }
        return null
    }

    data class CalculatedItem(
        val brandId: Int,
        val brandName: String,
        val totalQuantity: Double,
        val rate: Double,
        val amount: Double
    )

    fun calculateDailyProcurement(
        date: Date,
        allSubscriptions: List<com.example.milkiwai.data.entity.SubscriptionWithBrand>,
        allChanges: List<QuantityChange>,
        allVacations: List<Vacation>
    ): List<ProcurementItem> {
        val result = mutableListOf<ProcurementItem>()
        
        // Group subscriptions by brand for easier processing
        val brands = allSubscriptions.map { it.brand }.filterNotNull().distinctBy { it.id }
        
        for (brand in brands) {
            var totalQty = 0.0
            val brandSubs = allSubscriptions.filter { it.brand?.id == brand.id && it.subscription.isActive }
            
            for (subWithBrand in brandSubs) {
                val sub = subWithBrand.subscription
                val customerId = sub.customerId
                
                // 1. Check Pause for this customer
                val customerVacations = allVacations.filter { it.customerId == customerId }
                if (isDateInRange(date, customerVacations)) {
                    continue
                }
                
                // 2. Check Quantity Change for this customer and brand
                val customerChanges = allChanges.filter { it.customerId == customerId && it.brandId == brand.id }
                val change = findQuantityChange(date, brand.id, customerChanges)
                
                if (change != null) {
                    totalQty += change.newQuantity
                } else {
                    totalQty += sub.quantity
                }
            }
            
            if (totalQty > 0) {
                result.add(ProcurementItem(brand.id, brand.brandName, totalQty))
            }
        }
        
        return result
    }

    data class ProcurementItem(
        val brandId: Int,
        val brandName: String,
        val quantity: Double
    )
}
