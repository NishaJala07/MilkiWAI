package com.example.milkiwai.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.milkiwai.data.database.AppDatabase
import com.example.milkiwai.data.entity.Customer
import com.example.milkiwai.repository.CustomerRepository
import com.example.milkiwai.repository.FirebaseRepository
import kotlinx.coroutines.launch

class CustomerViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: CustomerRepository
    private val firebaseRepo = FirebaseRepository()
    val allCustomers: LiveData<List<Customer>>

    init {
        val dao = AppDatabase.getDatabase(application).customerDao()
        repository = CustomerRepository(dao)
        allCustomers = repository.allCustomers
    }

    suspend fun insertCustomer(customer: Customer): Long {
        val newId = repository.insert(customer)
        firebaseRepo.backupCustomers(listOf(customer.copy(id = newId.toInt())))
        return newId
    }

    fun insert(customer: Customer) {
        viewModelScope.launch {
            insertCustomer(customer)
        }
    }

    fun update(customer: Customer) {
        viewModelScope.launch {
            repository.update(customer)
            firebaseRepo.backupCustomers(listOf(customer))
        }
    }

    fun delete(customer: Customer) {
        viewModelScope.launch {
            repository.delete(customer)
        }
    }
}
