package com.example.milkiwai.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.milkiwai.data.entity.MonthlyBill

@Dao
interface MonthlyBillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: MonthlyBill): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bills: List<MonthlyBill>)

    @Update
    suspend fun updateBill(bill: MonthlyBill)

    @Query("SELECT * FROM monthly_bills WHERE id = :billId")
    suspend fun getBillById(billId: Int): MonthlyBill?

    @Query("UPDATE monthly_bills SET paymentStatus = :status WHERE id = :billId")
    suspend fun updatePaymentStatus(billId: Int, status: String)

    @Transaction
    @Query("SELECT * FROM monthly_bills ORDER BY year DESC, month DESC")
    fun getAllBillsWithCustomer(): LiveData<List<com.example.milkiwai.data.entity.BillWithCustomer>>

    @Query("SELECT * FROM monthly_bills WHERE customerId = :customerId ORDER BY year DESC, month DESC")
    fun getBillsByCustomer(customerId: Int): LiveData<List<MonthlyBill>>

    @Query("SELECT SUM(totalAmount) FROM monthly_bills WHERE billDate = :today")
    fun getDailyRevenue(today: String): LiveData<Double?>

    @Query("SELECT SUM(totalAmount) FROM monthly_bills WHERE month = :month AND year = :year")
    fun getMonthlyRevenue(month: Int, year: Int): LiveData<Double?>

    @Transaction
    @Query("SELECT * FROM monthly_bills WHERE paymentStatus = 'PENDING'")
    fun getPendingPayments(): LiveData<List<com.example.milkiwai.data.entity.BillWithCustomer>>

    @Query("SELECT COUNT(*) FROM monthly_bills WHERE paymentStatus = 'PENDING'")
    suspend fun getPendingBillsCount(): Int

    @Query("SELECT customerId, SUM(totalAmount) as totalRevenue FROM monthly_bills GROUP BY customerId ORDER BY totalRevenue DESC")
    fun getCustomerWiseRevenue(): LiveData<List<CustomerRevenue>>

    @Query("SELECT * FROM monthly_bills")
    suspend fun getAllBillsSync(): List<MonthlyBill>

    @Transaction
    @Query("SELECT * FROM monthly_bills WHERE paymentStatus = 'PENDING'")
    suspend fun getPendingPaymentsSync(): List<com.example.milkiwai.data.entity.BillWithCustomer>
}

data class CustomerRevenue(
    val customerId: Int,
    val totalRevenue: Double
)
