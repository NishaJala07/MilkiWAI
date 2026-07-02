package com.example.milkiwai.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.milkiwai.data.entity.DailyTransaction

@Dao
interface DailyTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: DailyTransaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<DailyTransaction>)

    @Update
    suspend fun update(transaction: DailyTransaction)

    @Delete
    suspend fun delete(transaction: DailyTransaction)

    @Query("SELECT * FROM daily_transactions WHERE transactionDate LIKE '%' || :monthAndYear")
    fun getTransactionsByMonth(monthAndYear: String): LiveData<List<DailyTransaction>>

    @Query("SELECT * FROM daily_transactions WHERE customerId = :customerId")
    fun getTransactionsByCustomer(customerId: Int): LiveData<List<DailyTransaction>>
    
    @Query("SELECT * FROM daily_transactions WHERE transactionDate = :date")
    suspend fun getTransactionsByDate(date: String): List<DailyTransaction>

    @Query("SELECT transactionDate FROM daily_transactions ORDER BY id DESC LIMIT 1")
    suspend fun getLastEntryDate(): String?
}
