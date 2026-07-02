package com.example.milkiwai.repository

import com.example.milkiwai.data.dao.QuantityChangeDao
import com.example.milkiwai.data.entity.QuantityChange

class QuantityChangeRepository(private val dao: QuantityChangeDao) {

    fun getChangesByCustomer(customerId: Int) = dao.getChangesByCustomer(customerId)

    suspend fun getChangesByCustomerList(customerId: Int) = dao.getChangesByCustomerList(customerId)

    suspend fun insert(change: QuantityChange) {
        dao.insertChange(change)
    }

    suspend fun update(change: QuantityChange) {
        dao.updateChange(change)
    }

    suspend fun delete(change: QuantityChange) {
        dao.deleteChange(change)
    }
}
