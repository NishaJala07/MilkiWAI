package com.example.milkiwai.repository

import com.example.milkiwai.data.entity.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getBaseDoc() = db.collection("users").document(auth.currentUser?.uid ?: "unknown")

    suspend fun backupCustomers(customers: List<Customer>) {
        val batch = db.batch()
        customers.forEach { customer ->
            batch.set(getBaseDoc().collection("customers").document(customer.id.toString()), customer)
        }
        batch.commit().await()
    }

    suspend fun backupBrands(brands: List<MilkBrand>) {
        val batch = db.batch()
        brands.forEach { brand ->
            batch.set(getBaseDoc().collection("milk_brands").document(brand.id.toString()), brand)
        }
        batch.commit().await()
    }

    suspend fun backupSubscriptions(subscriptions: List<CustomerSubscription>) {
        val batch = db.batch()
        subscriptions.forEach { sub ->
            batch.set(getBaseDoc().collection("subscriptions").document(sub.id.toString()), sub)
        }
        batch.commit().await()
    }

    suspend fun backupQuantityChanges(changes: List<QuantityChange>) {
        val batch = db.batch()
        changes.forEach { change ->
            batch.set(getBaseDoc().collection("quantity_changes").document(change.id.toString()), change)
        }
        batch.commit().await()
    }

    suspend fun backupVacations(vacations: List<Vacation>) {
        val batch = db.batch()
        vacations.forEach { vac ->
            batch.set(getBaseDoc().collection("vacations").document(vac.id.toString()), vac)
        }
        batch.commit().await()
    }

    suspend fun backupDeliveries(deliveries: List<Delivery>) {
        val batch = db.batch()
        deliveries.forEach { delivery ->
            batch.set(getBaseDoc().collection("deliveries").document(delivery.id.toString()), delivery)
        }
        batch.commit().await()
    }

    suspend fun backupNotifications(notifications: List<NotificationEntity>) {
        val batch = db.batch()
        notifications.forEach { note ->
            batch.set(getBaseDoc().collection("notifications").document(note.id.toString()), note)
        }
        batch.commit().await()
    }

    suspend fun backupStock(stocks: List<Stock>) {
        val batch = db.batch()
        stocks.forEach { stock ->
            batch.set(getBaseDoc().collection("stock").document(stock.id.toString()), stock)
        }
        batch.commit().await()
    }

    suspend fun backupBills(bills: List<MonthlyBill>, billItems: List<BillItem>) {
        val batch = db.batch()
        bills.forEach { bill ->
            batch.set(getBaseDoc().collection("monthly_bills").document(bill.id.toString()), bill)
        }
        billItems.forEach { item ->
            batch.set(getBaseDoc().collection("bill_items").document(item.id.toString()), item)
        }
        batch.commit().await()
    }

    suspend fun restoreCustomers(): List<Customer> {
        return getBaseDoc().collection("customers").get().await().toObjects(Customer::class.java)
    }

    suspend fun restoreBrands(): List<MilkBrand> {
        return getBaseDoc().collection("milk_brands").get().await().toObjects(MilkBrand::class.java)
    }

    suspend fun restoreSubscriptions(): List<CustomerSubscription> {
        return getBaseDoc().collection("subscriptions").get().await().toObjects(CustomerSubscription::class.java)
    }

    suspend fun restoreQuantityChanges(): List<QuantityChange> {
        return getBaseDoc().collection("quantity_changes").get().await().toObjects(QuantityChange::class.java)
    }

    suspend fun restoreVacations(): List<Vacation> {
        return getBaseDoc().collection("vacations").get().await().toObjects(Vacation::class.java)
    }

    suspend fun restoreDeliveries(): List<Delivery> {
        return getBaseDoc().collection("deliveries").get().await().toObjects(Delivery::class.java)
    }

    suspend fun restoreNotifications(): List<NotificationEntity> {
        return getBaseDoc().collection("notifications").get().await().toObjects(NotificationEntity::class.java)
    }

    suspend fun restoreStock(): List<Stock> {
        return getBaseDoc().collection("stock").get().await().toObjects(Stock::class.java)
    }

    suspend fun restoreBills(): List<MonthlyBill> {
        return getBaseDoc().collection("monthly_bills").get().await().toObjects(MonthlyBill::class.java)
    }
    
    suspend fun restoreBillItems(): List<BillItem> {
        return getBaseDoc().collection("bill_items").get().await().toObjects(BillItem::class.java)
    }
}
