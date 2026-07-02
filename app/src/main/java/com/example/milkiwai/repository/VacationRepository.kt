package com.example.milkiwai.repository

import com.example.milkiwai.data.dao.VacationDao
import com.example.milkiwai.data.entity.Vacation

class VacationRepository(private val dao: VacationDao) {
    suspend fun getVacationsByCustomerList(customerId: Int) = dao.getVacationsByCustomerList(customerId)
    fun getVacationsByCustomer(customerId: Int) = dao.getVacationsByCustomer(customerId)
    suspend fun insert(vacation: Vacation) = dao.insertVacation(vacation)
    suspend fun update(vacation: Vacation) = dao.updateVacation(vacation)
    suspend fun delete(vacation: Vacation) = dao.deleteVacation(vacation)
}
