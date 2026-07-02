package com.example.milkiwai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.milkiwai.data.database.AppDatabase
import com.example.milkiwai.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseRepo = FirebaseRepository()
    private val db = AppDatabase.getDatabase(application)
    
    val loading = MutableStateFlow(false)
    val syncStatus = MutableStateFlow("Pending Sync")

    fun backupNow() {
        viewModelScope.launch {
            loading.value = true
            try {
                val customers = db.customerDao().getAllCustomersList()
                firebaseRepo.backupCustomers(customers)

                val brands = db.milkBrandDao().getAllBrandsList() // Need to add this
                firebaseRepo.backupBrands(brands)

                val subscriptions = db.subscriptionDao().getAllSubscriptionsList() // Need to add this
                firebaseRepo.backupSubscriptions(subscriptions)

                val changes = db.quantityChangeDao().getAllChangesList() // Need to add this
                firebaseRepo.backupQuantityChanges(changes)

                val vacations = db.vacationDao().getAllVacationsList() // Need to add this
                firebaseRepo.backupVacations(vacations)

                val bills = db.monthlyBillDao().getAllBillsSync() // Need to add this
                val billItems = db.billItemDao().getAllItemsSync() // Need to add this
                firebaseRepo.backupBills(bills, billItems)

                syncStatus.value = "Synced"
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                loading.value = false
            }
        }
    }

    fun restoreNow() {
        viewModelScope.launch {
            loading.value = true
            try {
                val customers = firebaseRepo.restoreCustomers()
                db.customerDao().insertAll(customers)

                val brands = firebaseRepo.restoreBrands()
                db.milkBrandDao().insertAll(brands)

                val subs = firebaseRepo.restoreSubscriptions()
                db.subscriptionDao().insertAll(subs)

                val changes = firebaseRepo.restoreQuantityChanges()
                db.quantityChangeDao().insertAll(changes)

                val vacations = firebaseRepo.restoreVacations()
                db.vacationDao().insertAll(vacations)

                val bills = firebaseRepo.restoreBills()
                db.monthlyBillDao().insertAll(bills)

                val items = firebaseRepo.restoreBillItems()
                db.billItemDao().insertAll(items)

                syncStatus.value = "Synced"
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                loading.value = false
            }
        }
    }
}
