package com.example.milkiwai.repository

import com.example.milkiwai.data.dao.SubscriptionDao
import com.example.milkiwai.data.entity.CustomerSubscription

class SubscriptionRepository(private val dao: SubscriptionDao) {

    val allSubscriptions = dao.getAllSubscriptionsWithBrand()

    fun getSubscriptionsByCustomer(customerId: Int) = dao.getSubscriptionsByCustomer(customerId)

    suspend fun getSubscriptionsByCustomerList(customerId: Int) = dao.getSubscriptionsByCustomerList(customerId)

    suspend fun insert(subscription: CustomerSubscription) {
        dao.insertSubscription(subscription)
    }

    suspend fun update(subscription: CustomerSubscription) {
        dao.updateSubscription(subscription)
    }

    suspend fun delete(subscription: CustomerSubscription) {
        dao.deleteSubscription(subscription)
    }
}
