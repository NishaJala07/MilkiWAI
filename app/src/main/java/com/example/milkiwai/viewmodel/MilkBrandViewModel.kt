package com.example.milkiwai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.milkiwai.data.database.AppDatabase
import com.example.milkiwai.data.entity.MilkBrand
import com.example.milkiwai.repository.MilkBrandRepository
import com.example.milkiwai.repository.FirebaseRepository
import kotlinx.coroutines.launch

class MilkBrandViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: MilkBrandRepository
    private val firebaseRepo = FirebaseRepository()
    val allBrands: LiveData<List<MilkBrand>>

    init {
        val dao = AppDatabase
            .getDatabase(application)
            .milkBrandDao()
        repository = MilkBrandRepository(dao)
        allBrands = repository.allBrands
    }

    fun insert(brand: MilkBrand) {
        viewModelScope.launch {
            val newId = repository.insert(brand)
            firebaseRepo.backupBrands(listOf(brand.copy(id = newId.toInt())))
        }
    }

    fun update(brand: MilkBrand) {
        viewModelScope.launch {
            repository.update(brand)
            firebaseRepo.backupBrands(listOf(brand))
        }
    }

    fun delete(brand: MilkBrand) {
        viewModelScope.launch {
            repository.delete(brand)
        }
    }
}
