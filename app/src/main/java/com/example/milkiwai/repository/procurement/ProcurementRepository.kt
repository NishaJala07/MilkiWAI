package com.example.milkiwai.repository.procurement

import com.example.milkiwai.data.dao.*
import com.example.milkiwai.data.entity.MilkBrand
import com.example.milkiwai.data.model.ProcurementReport
import com.example.milkiwai.utils.BillingCalculator
import java.util.*

class ProcurementRepository(
    private val subscriptionDao: SubscriptionDao,
    private val changeDao: QuantityChangeDao,
    private val vacationDao: VacationDao,
    private val brandDao: MilkBrandDao
) {
    private val calculator = BillingCalculator()

    suspend fun getProcurementReport(date: Date, bufferPercentage: Double = 5.0): List<ProcurementReport> {
        val allSubs = subscriptionDao.getAllSubscriptionsWithBrandList()
        val allChanges = changeDao.getAllChangesList()
        val allVacations = vacationDao.getAllVacationsList()

        val dailyProcurement = calculator.calculateDailyProcurement(date, allSubs, allChanges, allVacations)

        return dailyProcurement.map { item ->
            val suggested = item.quantity * (1 + bufferPercentage / 100.0)
            ProcurementReport(
                brandId = item.brandId,
                brandName = item.brandName,
                requiredQuantity = item.quantity,
                suggestedQuantity = suggested
            )
        }
    }

    suspend fun getExpectedRevenue(date: Date): Double {
        val allSubs = subscriptionDao.getAllSubscriptionsWithBrandList()
        val allChanges = changeDao.getAllChangesList()
        val allVacations = vacationDao.getAllVacationsList()
        val allBrands = brandDao.getAllBrandsList().associateBy { it.id }

        val dailyProcurement = calculator.calculateDailyProcurement(date, allSubs, allChanges, allVacations)
        
        return dailyProcurement.sumOf { item ->
            val brand = allBrands[item.brandId]
            item.quantity * (brand?.pricePerLitre ?: 0.0)
        }
    }

    suspend fun getAverageDemand(days: Int): Double {
        val allSubs = subscriptionDao.getAllSubscriptionsWithBrandList()
        val allChanges = changeDao.getAllChangesList()
        val allVacations = vacationDao.getAllVacationsList()

        var totalQty = 0.0
        val calendar = Calendar.getInstance()
        val today = calendar.time
        
        for (i in 1..days) {
            calendar.time = today
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val daily = calculator.calculateDailyProcurement(calendar.time, allSubs, allChanges, allVacations)
            totalQty += daily.sumOf { it.quantity }
        }
        
        return if (days > 0) totalQty / days else 0.0
    }
    
    suspend fun getTotalExpectedDeliveries(date: Date): Int {
        val allSubs = subscriptionDao.getAllSubscriptionsWithBrandList()
        val allChanges = changeDao.getAllChangesList()
        val allVacations = vacationDao.getAllVacationsList()
        
        val activeCustomers = mutableSetOf<Int>()
        
        for (subWithBrand in allSubs) {
            val sub = subWithBrand.subscription
            val customerId = sub.customerId
            
            // 1. Check Pause
            val customerVacations = allVacations.filter { it.customerId == customerId }
            if (isDateInRange(date, customerVacations)) {
                continue
            }
            
            // 2. Check Quantity Change (if newQty is 0, they don't get milk that day)
            // Wait, calculateDailyProcurement doesn't return count of customers.
            // Let's implement logic here.
            
            val customerChanges = allChanges.filter { it.customerId == customerId && it.brandId == sub.brandId }
            val change = findQuantityChange(date, sub.brandId, customerChanges)
            
            if (change != null) {
                if (change.newQuantity > 0) activeCustomers.add(customerId)
            } else {
                if (sub.quantity > 0) activeCustomers.add(customerId)
            }
        }
        
        return activeCustomers.size
    }

    // Helper methods similar to BillingCalculator but private here for consistency
    private fun isDateInRange(date: Date, vacations: List<com.example.milkiwai.data.entity.Vacation>): Boolean {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        for (v in vacations) {
            try {
                val start = sdf.parse(v.startDate)
                val end = sdf.parse(v.endDate)
                if (date.compareTo(start) >= 0 && date.compareTo(end) <= 0) {
                    return true
                }
            } catch (e: Exception) {}
        }
        return false
    }

    private fun findQuantityChange(date: Date, brandId: Int, changes: List<com.example.milkiwai.data.entity.QuantityChange>): com.example.milkiwai.data.entity.QuantityChange? {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        for (c in changes) {
            if (c.brandId == brandId && c.status == "ACTIVE") {
                try {
                    val start = sdf.parse(c.startDate)
                    val end = sdf.parse(c.endDate)
                    if (date.compareTo(start) >= 0 && date.compareTo(end) <= 0) {
                        return c
                    }
                } catch (e: Exception) {}
            }
        }
        return null
    }
}
