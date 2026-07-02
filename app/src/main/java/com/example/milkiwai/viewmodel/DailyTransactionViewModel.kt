package com.example.milkiwai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.milkiwai.data.database.AppDatabase
import com.example.milkiwai.data.entity.DailyTransaction
import com.example.milkiwai.repository.DailyTransactionRepository
import kotlinx.coroutines.launch

class DailyTransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DailyTransactionRepository
    
    private val _lastEntryDate = MutableLiveData<String?>()
    val lastEntryDate: LiveData<String?> = _lastEntryDate

    init {
        val dao = AppDatabase.getDatabase(application).dailyTransactionDao()
        repository = DailyTransactionRepository(dao)
        fetchLastEntryDate()
    }

    fun insertAll(transactions: List<DailyTransaction>) {
        viewModelScope.launch {
            repository.insertAll(transactions)
            fetchLastEntryDate()
        }
    }

    fun getTransactionsByMonth(monthYear: String) = repository.getTransactionsByMonth(monthYear)

    fun getTransactionsByDate(date: String, callback: (List<DailyTransaction>) -> Unit) {
        viewModelScope.launch {
            callback(repository.getTransactionsByDate(date))
        }
    }

    private fun fetchLastEntryDate() {
        viewModelScope.launch {
            _lastEntryDate.postValue(repository.getLastEntryDate())
        }
    }
}
