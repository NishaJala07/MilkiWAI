package com.example.milkiwai.ui.register

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.data.entity.DailyTransaction
import com.example.milkiwai.viewmodel.CustomerViewModel
import com.example.milkiwai.viewmodel.DailyTransactionViewModel
import com.example.milkiwai.viewmodel.SubscriptionViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class HistoricalEntryActivity : AppCompatActivity() {

    private val customerViewModel: CustomerViewModel by viewModels()
    private val transactionViewModel: DailyTransactionViewModel by viewModels()
    private val subscriptionViewModel: SubscriptionViewModel by viewModels()
    
    private lateinit var adapter: BulkEntryAdapter
    private var selectedDate: Date = Date()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historical_entry)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val rvBulk = findViewById<RecyclerView>(R.id.rvBulkEntry)
        adapter = BulkEntryAdapter(this)
        rvBulk.adapter = adapter
        rvBulk.layoutManager = LinearLayoutManager(this)

        updateDateDisplay()
        detectMissingEntries()

        findViewById<View>(R.id.btnChangeDate).setOnClickListener { showDatePicker() }
        findViewById<View>(R.id.cardDatePicker).setOnClickListener { showDatePicker() }

        loadDataForDate()

        findViewById<com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton>(R.id.btnSaveAll).setOnClickListener {
            saveAllEntries()
        }
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Entry Date")
            .setSelection(selectedDate.time)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            selectedDate = Date(selection)
            updateDateDisplay()
            loadDataForDate()
        }
        picker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun updateDateDisplay() {
        findViewById<TextView>(R.id.tvSelectedDate).text = dateFormat.format(selectedDate)
    }

    private fun loadDataForDate() {
        val dateStr = dateFormat.format(selectedDate)
        
        transactionViewModel.getTransactionsByDate(dateStr) { existing ->
            customerViewModel.allCustomers.observe(this@HistoricalEntryActivity) { customers ->
                subscriptionViewModel.allSubscriptions.observe(this@HistoricalEntryActivity) { subs ->
                    val entryItems = mutableListOf<BulkEntryItem>()
                    
                    for (customer in customers) {
                        val activeSubs = subs.filter { it.customer?.id == customer.id && it.subscription.isActive }
                        
                        if (activeSubs.isEmpty()) {
                            // Even if no active sub, check if there was a manual transaction
                            val manualTransactions = existing.filter { it.customerId == customer.id }
                            if (manualTransactions.isNotEmpty()) {
                                for (trans in manualTransactions) {
                                    val brandName = subs.find { it.brand?.id == trans.brandId }?.brand?.brandName ?: "Unknown"
                                    entryItems.add(BulkEntryItem(
                                        customerId = customer.id,
                                        customerName = customer.name,
                                        brandId = trans.brandId,
                                        brandName = brandName,
                                        quantity = trans.quantity
                                    ))
                                }
                            }
                        } else {
                            for (sub in activeSubs) {
                                val existingTrans = existing.find { 
                                    it.customerId == customer.id && it.brandId == sub.brand?.id 
                                }
                                
                                entryItems.add(BulkEntryItem(
                                    customerId = customer.id,
                                    customerName = customer.name,
                                    brandId = sub.brand?.id ?: -1,
                                    brandName = sub.brand?.brandName ?: "Unknown",
                                    quantity = existingTrans?.quantity ?: sub.subscription.quantity
                                ))
                            }
                        }
                    }
                    adapter.setItems(entryItems)
                }
            }
        }
    }

    private fun detectMissingEntries() {
        transactionViewModel.lastEntryDate.observe(this) { lastDateStr ->
            if (lastDateStr != null) {
                try {
                    val lastDate = dateFormat.parse(lastDateStr) ?: return@observe
                    val calendar = Calendar.getInstance()
                    calendar.time = lastDate
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    
                    val today = Calendar.getInstance()
                    today.set(Calendar.HOUR_OF_DAY, 0)
                    today.set(Calendar.MINUTE, 0)
                    today.set(Calendar.SECOND, 0)
                    today.set(Calendar.MILLISECOND, 0)
                    
                    val diff = today.timeInMillis - calendar.timeInMillis
                    val days = TimeUnit.MILLISECONDS.toDays(diff)
                    
                    if (days > 1) {
                        val tvMissing = findViewById<TextView>(R.id.tvMissingDetection)
                        tvMissing.visibility = View.VISIBLE
                        tvMissing.text = "${days - 1} Days Missing Since Last Entry ($lastDateStr)"
                    } else {
                        findViewById<TextView>(R.id.tvMissingDetection).visibility = View.GONE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun saveAllEntries() {
        val items = adapter.getItems()
        val dateStr = dateFormat.format(selectedDate)
        
        val transactions = items.map { item ->
            DailyTransaction(
                customerId = item.customerId,
                brandId = item.brandId,
                transactionDate = dateStr,
                quantity = item.quantity
            )
        }
        
        transactionViewModel.insertAll(transactions)
        Toast.makeText(this, "All entries saved successfully", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onDestroy() {
        adapter.onDestroy()
        super.onDestroy()
    }

    data class BulkEntryItem(
        val customerId: Int,
        val customerName: String,
        val brandId: Int,
        val brandName: String,
        var quantity: Double
    )
}
