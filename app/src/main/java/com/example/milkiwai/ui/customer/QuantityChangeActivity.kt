package com.example.milkiwai.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.data.entity.*
import com.example.milkiwai.viewmodel.QuantityChangeViewModel
import com.example.milkiwai.viewmodel.SubscriptionViewModel
import com.example.milkiwai.utils.VoiceInputHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class QuantityChangeActivity : AppCompatActivity() {

    private val changeViewModel: QuantityChangeViewModel by viewModels()
    private val subscriptionViewModel: SubscriptionViewModel by viewModels()
    private lateinit var voiceInputHelper: VoiceInputHelper

    private var customerId: Int = -1
    private var customerName: String = ""
    private var customerSubscriptions: List<SubscriptionWithBrand> = emptyList()

    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quantity_change)

        customerId = intent.getIntExtra("CUSTOMER_ID", -1)
        customerName = intent.getStringExtra("CUSTOMER_NAME") ?: ""

        if (customerId == -1) {
            finish()
            return
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.title = "Changes: $customerName"
        
        voiceInputHelper = VoiceInputHelper(this)

        val rvChanges = findViewById<RecyclerView>(R.id.rvQuantityChanges)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddChange)

        val adapter = QuantityChangeAdapter(
            onEdit = { item -> showChangeDialog(item.quantityChange) },
            onDelete = { item -> showDeleteConfirmation(item.quantityChange, item.brand.brandName) }
        )
        rvChanges.adapter = adapter
        rvChanges.layoutManager = LinearLayoutManager(this)

        changeViewModel.getChangesByCustomer(customerId).observe(this) { changes ->
            adapter.setChanges(changes)
        }

        subscriptionViewModel.getSubscriptions(customerId).observe(this) { subscriptions ->
            customerSubscriptions = subscriptions
        }

        fabAdd.setOnClickListener {
            if (customerSubscriptions.isEmpty()) {
                Toast.makeText(this, "Customer has no active subscriptions", Toast.LENGTH_SHORT).show()
            } else {
                showChangeDialog(null)
            }
        }
    }

    private fun showChangeDialog(existingChange: QuantityChange?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_quantity_change, null)
        val brandAutoComplete = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerBrands)
        val etCurrentQty = dialogView.findViewById<EditText>(R.id.etCurrentQty)
        val etNewQty = dialogView.findViewById<EditText>(R.id.etNewQty)
        val etStartDate = dialogView.findViewById<EditText>(R.id.etStartDate)
        val etEndDate = dialogView.findViewById<EditText>(R.id.etEndDate)
        val etRemarks = dialogView.findViewById<EditText>(R.id.etRemarks)
        val statusAutoComplete = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerStatus)

        // Voice Input
        voiceInputHelper.attachTo(dialogView.findViewById(R.id.tilNewQty))
        voiceInputHelper.attachTo(dialogView.findViewById(R.id.tilRemarks))

        val brandNames = customerSubscriptions.map { it.brand.brandName }
        val brandAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, brandNames)
        brandAutoComplete.setAdapter(brandAdapter)

        val statuses = listOf("ACTIVE", "COMPLETED", "CANCELLED")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statuses)
        statusAutoComplete.setAdapter(statusAdapter)
        statusAutoComplete.setText(statuses[0], false)

        brandAutoComplete.setOnItemClickListener { _, _, position, _ ->
            etCurrentQty.setText(customerSubscriptions[position].subscription.quantity.toString())
        }

        if (existingChange != null) {
            val brandIndex = customerSubscriptions.indexOfFirst { it.brand.id == existingChange.brandId }
            if (brandIndex != -1) {
                brandAutoComplete.setText(brandNames[brandIndex], false)
                etCurrentQty.setText(existingChange.oldQuantity.toString())
            }
            etNewQty.setText(existingChange.newQuantity.toString())
            etStartDate.setText(existingChange.startDate)
            etEndDate.setText(existingChange.endDate)
            etRemarks.setText(existingChange.remarks)
            statusAutoComplete.setText(existingChange.status, false)
        }

        etStartDate.setOnClickListener { showDatePicker { date -> etStartDate.setText(date) } }
        etEndDate.setOnClickListener { showDatePicker { date -> etEndDate.setText(date) } }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("SAVE") { _, _ ->
                val selectedBrandName = brandAutoComplete.text.toString()
                val selectedIndex = brandNames.indexOf(selectedBrandName)
                
                if (selectedIndex == -1) {
                    Toast.makeText(this, "Please select a valid brand", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val brand = customerSubscriptions[selectedIndex].brand
                val newQty = etNewQty.text.toString().toDoubleOrNull() ?: 0.0
                val start = etStartDate.text.toString()
                val end = etEndDate.text.toString()
                val remarks = etRemarks.text.toString()
                val status = statusAutoComplete.text.toString()

                if (start.isNotEmpty() && end.isNotEmpty() && newQty > 0) {
                    val change = QuantityChange(
                        id = existingChange?.id ?: 0,
                        customerId = customerId,
                        brandId = brand.id,
                        oldQuantity = customerSubscriptions[selectedIndex].subscription.quantity,
                        newQuantity = newQty,
                        startDate = start,
                        endDate = end,
                        remarks = remarks,
                        status = status
                    )
                    if (existingChange == null) changeViewModel.insert(change) else changeViewModel.update(change)
                } else {
                    Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()
        picker.addOnPositiveButtonClickListener { selection ->
            onDateSelected(dateFormatter.format(Date(selection)))
        }
        picker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun showDeleteConfirmation(change: QuantityChange, brandName: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Quantity Change?")
            .setMessage("Are you sure you want to delete change for $brandName?")
            .setPositiveButton("YES") { _, _ ->
                changeViewModel.delete(change)
            }
            .setNegativeButton("NO", null)
            .show()
    }

    override fun onDestroy() {
        voiceInputHelper.destroy()
        super.onDestroy()
    }
}
