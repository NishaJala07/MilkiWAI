package com.example.milkiwai.ui.customer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.data.entity.CustomerSubscription
import com.example.milkiwai.data.entity.MilkBrand
import com.example.milkiwai.viewmodel.MilkBrandViewModel
import com.example.milkiwai.viewmodel.SubscriptionViewModel
import com.google.android.material.appbar.MaterialToolbar

class CustomerSubscriptionActivity : AppCompatActivity() {

    private val subscriptionViewModel: SubscriptionViewModel by viewModels()
    private val brandViewModel: MilkBrandViewModel by viewModels()
    
    private var customerId: Int = -1
    private var customerName: String = ""
    private var availableBrands: List<MilkBrand> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_subscription)

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

        findViewById<TextView>(R.id.tvCustomerName).text = customerName

        val rvSubscriptions = findViewById<RecyclerView>(R.id.rvSubscriptions)
        val btnAddSubscription = findViewById<Button>(R.id.btnAddSubscriptionInline)
        val btnQuantityChanges = findViewById<Button>(R.id.btnQuantityChanges)
        val btnVacations = findViewById<Button>(R.id.btnVacations)

        val adapter = SubscriptionAdapter(
            onEdit = { item -> showSubscriptionDialog(item.subscription) },
            onDelete = { item -> showDeleteConfirmation(item.subscription, item.brand.brandName) }
        )
        rvSubscriptions.adapter = adapter
        rvSubscriptions.layoutManager = LinearLayoutManager(this)

        subscriptionViewModel.getSubscriptions(customerId).observe(this) { subscriptions ->
            adapter.setSubscriptions(subscriptions)
        }

        brandViewModel.allBrands.observe(this) { brands ->
            availableBrands = brands
        }

        btnAddSubscription.setOnClickListener {
            if (availableBrands.isEmpty()) {
                Toast.makeText(this, "Please add milk brands first", Toast.LENGTH_SHORT).show()
            } else {
                showSubscriptionDialog(null)
            }
        }

        btnQuantityChanges.setOnClickListener {
            val intent = Intent(this, QuantityChangeActivity::class.java).apply {
                putExtra("CUSTOMER_ID", customerId)
                putExtra("CUSTOMER_NAME", customerName)
            }
            startActivity(intent)
        }

        btnVacations.setOnClickListener {
            val intent = Intent(this, VacationActivity::class.java).apply {
                putExtra("CUSTOMER_ID", customerId)
                putExtra("CUSTOMER_NAME", customerName)
            }
            startActivity(intent)
        }
    }

    private fun showSubscriptionDialog(existingSubscription: CustomerSubscription?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_subscription, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerBrands)
        val etQuantity = dialogView.findViewById<EditText>(R.id.etQuantity)

        val brandNames = availableBrands.map { it.brandName }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, brandNames)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        if (existingSubscription != null) {
            val brandIndex = availableBrands.indexOfFirst { it.id == existingSubscription.brandId }
            if (brandIndex != -1) spinner.setSelection(brandIndex)
            etQuantity.setText(existingSubscription.quantity.toString())
        }

        AlertDialog.Builder(this)
            .setTitle(if (existingSubscription == null) "Add Subscription" else "Edit Subscription")
            .setView(dialogView)
            .setPositiveButton("SAVE") { _, _ ->
                val selectedBrandIndex = spinner.selectedItemPosition
                val quantityStr = etQuantity.text.toString().trim()

                if (quantityStr.isNotEmpty()) {
                    val quantity = quantityStr.toDoubleOrNull() ?: 0.0
                    val brandId = availableBrands[selectedBrandIndex].id
                    
                    if (existingSubscription == null) {
                        subscriptionViewModel.insert(CustomerSubscription(customerId = customerId, brandId = brandId, quantity = quantity))
                    } else {
                        subscriptionViewModel.update(existingSubscription.copy(brandId = brandId, quantity = quantity))
                    }
                } else {
                    Toast.makeText(this, "Enter quantity", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation(subscription: CustomerSubscription, brandName: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Subscription?")
            .setMessage("Are you sure you want to delete $brandName subscription?")
            .setPositiveButton("YES") { _, _ ->
                subscriptionViewModel.delete(subscription)
            }
            .setNegativeButton("NO", null)
            .show()
    }
}
