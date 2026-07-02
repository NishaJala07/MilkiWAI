package com.example.milkiwai.repository

import androidx.lifecycle.LiveData
import com.example.milkiwai.data.dao.DailyTransactionDao
import com.example.milkiwai.data.entity.DailyTransaction

class DailyTransactionRepository(private val dao: DailyTransactionDao) {

    suspend fun insert(transaction: DailyTransaction) = dao.insert(transaction)

    suspend fun insertAll(transactions: List<DailyTransaction>) = dao.insertAll(transactions)

    suspend fun update(transaction: DailyTransaction) = dao.update(transaction)

    suspend fun delete(transaction: DailyTransaction) = dao.delete(transaction)

    fun getTransactionsByMonth(monthYear: String): LiveData<List<DailyTransaction>> = 
        dao.getTransactionsByMonth("%$monthYear")

    fun getTransactionsByCustomer(customerId: Int): LiveData<List<DailyTransaction>> = 
        dao.getTransactionsByCustomer(customerId)

    suspend fun getTransactionsByDate(date: String) = dao.getTransactionsByDate(date)

    suspend fun getLastEntryDate() = dao.getLastEntryDate()
}
