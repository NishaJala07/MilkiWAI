package com.example.milkiwai.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.milkiwai.data.entity.Delivery

@Dao
interface DeliveryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(delivery: Delivery): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(deliveries: List<Delivery>)

    @Update
    suspend fun update(delivery: Delivery)

    @Delete
    suspend fun delete(delivery: Delivery)

    @Query("SELECT * FROM deliveries WHERE deliveryDate = :date")
    fun getDeliveriesByDate(date: String): LiveData<List<Delivery>>

    @Query("SELECT * FROM deliveries WHERE customerId = :customerId AND deliveryDate = :date")
    suspend fun getDeliveryByCustomerAndDate(customerId: Int, date: String): List<Delivery>

    @Query("SELECT * FROM deliveries WHERE customerId = :customerId")
    fun getDeliveriesByCustomer(customerId: Int): LiveData<List<Delivery>>

    @Query("SELECT * FROM deliveries WHERE deliveryDate = :date AND status = :status")
    suspend fun getDeliveriesByStatus(date: String, status: String): List<Delivery>

    @Query("SELECT * FROM deliveries WHERE deliveryDate BETWEEN :startDate AND :endDate")
    suspend fun getDeliveriesInRange(startDate: String, endDate: String): List<Delivery>
}
