package com.example.milkiwai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.milkiwai.data.database.AppDatabase
import com.example.milkiwai.data.entity.CustomerSubscription
import com.example.milkiwai.data.entity.SubscriptionWithBrand
import com.example.milkiwai.repository.SubscriptionRepository
import com.example.milkiwai.repository.FirebaseRepository
import kotlinx.coroutines.launch

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SubscriptionRepository
    private val firebaseRepo = FirebaseRepository()
    val allSubscriptions: LiveData<List<SubscriptionWithBrand>>

    init {
        val dao = AppDatabase.getDatabase(application).subscriptionDao()
        repository = SubscriptionRepository(dao)
        allSubscriptions = repository.allSubscriptions
    }

    fun getSubscriptions(customerId: Int) = repository.getSubscriptionsByCustomer(customerId)

    fun insert(subscription: CustomerSubscription) {
        viewModelScope.launch {
            repository.insert(subscription)
            firebaseRepo.backupSubscriptions(listOf(subscription))
        }
    }

    fun update(subscription: CustomerSubscription) {
        viewModelScope.launch {
            repository.update(subscription)
            firebaseRepo.backupSubscriptions(listOf(subscription))
        }
    }

    fun delete(subscription: CustomerSubscription) {
        viewModelScope.launch {
            repository.delete(subscription)
        }
    }
}
