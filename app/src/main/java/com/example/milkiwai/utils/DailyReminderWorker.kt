package com.example.milkiwai.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.milkiwai.data.database.AppDatabase
import com.example.milkiwai.repository.procurement.ProcurementRepository
import java.util.*

class DailyReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val notificationHelper = NotificationHelper(applicationContext)
        
        val procurementRepository = ProcurementRepository(
            db.subscriptionDao(),
            db.quantityChangeDao(),
            db.vacationDao(),
            db.milkBrandDao()
        )

        // 1. Check Tomorrow's Requirement
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time
        val report = procurementRepository.getProcurementReport(tomorrow)
        val totalRequired = report.sumOf { it.requiredQuantity }
        
        notificationHelper.showNotification(
            "Tomorrow's Requirement",
            "Total Milk Required for tomorrow: $totalRequired L"
        )

        // 2. Check Low Stock
        val totalAvailable = db.stockDao().getTotalStock() ?: 0.0
        if (totalRequired > totalAvailable) {
            notificationHelper.showNotification(
                "Low Stock Alert!",
                "You need ${totalRequired - totalAvailable} L more milk for tomorrow."
            )
        }

        // 3. Check Pending Payments
        val pendingBills = db.monthlyBillDao().getPendingPaymentsSync()
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        val todayDate = java.util.Calendar.getInstance().time

        pendingBills.forEach { item ->
            try {
                val billDate = sdf.parse(item.bill.billDate)
                if (billDate != null) {
                    val diff = todayDate.time - billDate.time
                    val days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diff)
                    
                    if (days >= 7) {
                        notificationHelper.showNotification(
                            "Payment Reminder",
                            "Bill for ${item.customer.name} (₹${item.bill.totalAmount}) is pending for $days days."
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return Result.success()
    }
}
