package com.example.milkiwai.repository

import com.example.milkiwai.data.dao.CustomerDao
import com.example.milkiwai.data.entity.Customer

class CustomerRepository(
    private val customerDao: CustomerDao
) {
    val allCustomers = customerDao.getAllCustomers()

    suspend fun getAllCustomersList() = customerDao.getAllCustomersList()

    suspend fun insert(customer: Customer): Long {
        return customerDao.insertCustomer(customer)
    }

    suspend fun update(customer: Customer) {
        customerDao.updateCustomer(customer)
    }

    suspend fun delete(customer: Customer) {
        customerDao.deleteCustomer(customer)
    }
}
