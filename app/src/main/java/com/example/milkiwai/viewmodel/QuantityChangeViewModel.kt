package com.example.milkiwai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.milkiwai.data.database.AppDatabase
import com.example.milkiwai.data.entity.QuantityChange
import com.example.milkiwai.repository.QuantityChangeRepository
import com.example.milkiwai.repository.FirebaseRepository
import kotlinx.coroutines.launch

class QuantityChangeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QuantityChangeRepository
    private val firebaseRepo = FirebaseRepository()

    init {
        val dao = AppDatabase.getDatabase(application).quantityChangeDao()
        repository = QuantityChangeRepository(dao)
    }

    fun getChangesByCustomer(customerId: Int) = repository.getChangesByCustomer(customerId)

    fun insert(change: QuantityChange) {
        viewModelScope.launch {
            repository.insert(change)
            firebaseRepo.backupQuantityChanges(listOf(change))
        }
    }

    fun update(change: QuantityChange) {
        viewModelScope.launch {
            repository.update(change)
            firebaseRepo.backupQuantityChanges(listOf(change))
        }
    }

    fun delete(change: QuantityChange) {
        viewModelScope.launch {
            repository.delete(change)
        }
    }
}
