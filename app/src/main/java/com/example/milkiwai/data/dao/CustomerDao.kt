package com.example.milkiwai.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.milkiwai.data.entity.Customer

@Dao
interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<Customer>)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("SELECT * FROM customers")
    fun getAllCustomers(): LiveData<List<Customer>>

    @Query("SELECT * FROM customers")
    suspend fun getAllCustomersList(): List<Customer>
}
