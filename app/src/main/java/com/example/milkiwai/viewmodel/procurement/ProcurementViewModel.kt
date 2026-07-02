package com.example.milkiwai.viewmodel.procurement

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.milkiwai.data.database.AppDatabase
import com.example.milkiwai.data.model.ProcurementReport
import com.example.milkiwai.repository.procurement.ProcurementRepository
import kotlinx.coroutines.launch
import java.util.*

class ProcurementViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProcurementRepository
    
    private val _procurementReport = MutableLiveData<List<ProcurementReport>>()
    val procurementReport: LiveData<List<ProcurementReport>> = _procurementReport

    private val _expectedRevenue = MutableLiveData<Double>()
    val expectedRevenue: LiveData<Double> = _expectedRevenue
    
    private val _totalDeliveries = MutableLiveData<Int>()
    val totalDeliveries: LiveData<Int> = _totalDeliveries

    private val _averageDemand = MutableLiveData<Double>()
    val averageDemand: LiveData<Double> = _averageDemand

    private val _stockShortage = MutableLiveData<Double>()
    val stockShortage: LiveData<Double> = _stockShortage

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ProcurementRepository(
            db.subscriptionDao(),
            db.quantityChangeDao(),
            db.vacationDao(),
            db.milkBrandDao()
        )
    }

    fun loadProcurementData(date: Date, bufferPercentage: Double = 5.0) {
        viewModelScope.launch {
            val report = repository.getProcurementReport(date, bufferPercentage)
            _procurementReport.value = report
            _expectedRevenue.value = repository.getExpectedRevenue(date)
            _totalDeliveries.value = repository.getTotalExpectedDeliveries(date)
            _averageDemand.value = repository.getAverageDemand(30)
            
            val db = AppDatabase.getDatabase(getApplication())
            val totalAvailable = db.stockDao().getTotalStock() ?: 0.0
            val totalRequired = report.sumOf { it.requiredQuantity }
            
            _stockShortage.value = if (totalRequired > totalAvailable) totalRequired - totalAvailable else 0.0
        }
    }
}
