package com.example.milkiwai.ui.billing

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.milkiwai.R
import com.example.milkiwai.viewmodel.BillViewModel
import com.google.android.material.appbar.MaterialToolbar
import java.util.*

class GenerateBillActivity : AppCompatActivity() {

    private val viewModel: BillViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_bill)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val spinnerMonth = findViewById<Spinner>(R.id.spinnerMonth)
        val spinnerYear = findViewById<Spinner>(R.id.spinnerYear)
        val btnGenerate = findViewById<Button>(R.id.btnGenerate)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        val months = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = monthAdapter

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = arrayOf(currentYear - 1, currentYear, currentYear + 1)
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYear.adapter = yearAdapter
        spinnerYear.setSelection(1) // Default to current year

        btnGenerate.setOnClickListener {
            val selectedMonth = spinnerMonth.selectedItemPosition
            val selectedYear = spinnerYear.selectedItem as Int

            btnGenerate.isEnabled = false
            progressBar.visibility = View.VISIBLE

            viewModel.generateBills(selectedMonth, selectedYear) {
                btnGenerate.isEnabled = true
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Bills generated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
