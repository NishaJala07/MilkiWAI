package com.example.milkiwai.repository

import androidx.lifecycle.LiveData
import com.example.milkiwai.data.dao.DeliveryDao
import com.example.milkiwai.data.entity.Delivery

class DeliveryRepository(private val deliveryDao: DeliveryDao) {

    suspend fun insert(delivery: Delivery) = deliveryDao.insert(delivery)

    suspend fun insertAll(deliveries: List<Delivery>) = deliveryDao.insertAll(deliveries)

    suspend fun update(delivery: Delivery) = deliveryDao.update(delivery)

    suspend fun delete(delivery: Delivery) = deliveryDao.delete(delivery)

    fun getDeliveriesByDate(date: String): LiveData<List<Delivery>> = deliveryDao.getDeliveriesByDate(date)

    suspend fun getDeliveryByCustomerAndDate(customerId: Int, date: String) = 
        deliveryDao.getDeliveryByCustomerAndDate(customerId, date)

    fun getDeliveriesByCustomer(customerId: Int): LiveData<List<Delivery>> = 
        deliveryDao.getDeliveriesByCustomer(customerId)
}
