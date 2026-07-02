package com.example.milkiwai.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.milkiwai.data.entity.CustomerSubscription
import com.example.milkiwai.data.entity.SubscriptionWithBrand

@Dao
interface SubscriptionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: CustomerSubscription)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subscriptions: List<CustomerSubscription>)

    @Update
    suspend fun updateSubscription(subscription: CustomerSubscription)

    @Delete
    suspend fun deleteSubscription(subscription: CustomerSubscription)

    @Transaction
    @Query("SELECT * FROM customer_subscriptions WHERE customerId = :customerId")
    fun getSubscriptionsByCustomer(customerId: Int): LiveData<List<SubscriptionWithBrand>>

    @Transaction
    @Query("SELECT * FROM customer_subscriptions WHERE customerId = :customerId")
    suspend fun getSubscriptionsByCustomerList(customerId: Int): List<SubscriptionWithBrand>

    @Query("SELECT * FROM customer_subscriptions")
    suspend fun getAllSubscriptionsList(): List<CustomerSubscription>

    @Transaction
    @Query("SELECT * FROM customer_subscriptions")
    fun getAllSubscriptionsWithBrand(): LiveData<List<SubscriptionWithBrand>>

    @Transaction
    @Query("SELECT * FROM customer_subscriptions")
    suspend fun getAllSubscriptionsWithBrandList(): List<SubscriptionWithBrand>
}
