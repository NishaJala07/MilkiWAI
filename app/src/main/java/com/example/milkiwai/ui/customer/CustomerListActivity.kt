package com.example.milkiwai.ui.customer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.data.entity.Customer
import com.example.milkiwai.viewmodel.CustomerViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class CustomerListActivity : AppCompatActivity() {

    private lateinit var viewModel: CustomerViewModel
    private lateinit var adapter: CustomerAdapter
    private var allCustomersList: List<Customer> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_list)

        setupToolbar()
        setupRecyclerView()
        setupSearchView()
        setupFilters()
        setupFAB()

        viewModel = ViewModelProvider(this).get(CustomerViewModel::class.java)
        viewModel.allCustomers.observe(this) { customers ->
            allCustomersList = customers
            updateUI(customers)
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.rvCustomers)
        adapter = CustomerAdapter(
            onViewClick = { customer ->
                val intent = Intent(this, CustomerSubscriptionActivity::class.java).apply {
                    putExtra("CUSTOMER_ID", customer.id)
                    putExtra("CUSTOMER_NAME", customer.name)
                }
                startActivity(intent)
            },
            onEditClick = { customer ->
                val intent = Intent(this, AddCustomerActivity::class.java).apply {
                    putExtra("CUSTOMER_ID", customer.id)
                    putExtra("IS_EDIT", true)
                }
                startActivity(intent)
            },
            onDeleteClick = { customer ->
                showDeleteConfirmationDialog(customer)
            }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupSearchView() {
        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                applyFilters()
                return true
            }
        })
    }

    private fun setupFilters() {
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupFilters)
        chipGroup.setOnCheckedStateChangeListener { _, _ ->
            applyFilters()
        }
    }

    private fun setupFAB() {
        findViewById<ExtendedFloatingActionButton>(R.id.fabAddCustomer).setOnClickListener {
            startActivity(Intent(this, AddCustomerActivity::class.java))
        }
    }

    private fun applyFilters() {
        val searchView = findViewById<SearchView>(R.id.searchView)
        val query = searchView.query.toString()
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupFilters)
        
        var filteredList = allCustomersList

        // Apply Search Filter
        if (query.isNotBlank()) {
            filteredList = filteredList.filter { customer ->
                customer.name.contains(query, ignoreCase = true) ||
                customer.buildingName.contains(query, ignoreCase = true) ||
                customer.roomNo.contains(query, ignoreCase = true) ||
                customer.mobile.contains(query)
            }
        }

        // Apply Status Filter (Placeholder for now since Customer entity lacks status)
        when (chipGroup.checkedChipId) {
            R.id.chipActive -> {
                // TODO: Logic for active customers
            }
            R.id.chipPaused -> {
                // TODO: Logic for paused customers
            }
        }

        adapter.setCustomers(filteredList)
        findViewById<View>(R.id.emptyState).visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateUI(customers: List<Customer>) {
        val emptyState = findViewById<View>(R.id.emptyState)
        val recyclerView = findViewById<RecyclerView>(R.id.rvCustomers)
        
        if (customers.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.setCustomers(customers)
        }
    }

    private fun showDeleteConfirmationDialog(customer: Customer) {
        AlertDialog.Builder(this)
            .setTitle("Delete Customer?")
            .setMessage("Customer: ${customer.name}\nAre you sure you want to delete this customer?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.delete(customer)
                Toast.makeText(this, "Customer deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
