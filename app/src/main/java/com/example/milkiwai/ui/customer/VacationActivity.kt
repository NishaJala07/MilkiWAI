package com.example.milkiwai.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.data.entity.Vacation
import com.example.milkiwai.viewmodel.BillViewModel
import com.example.milkiwai.utils.VoiceInputHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class VacationActivity : AppCompatActivity() {

    // Using BillViewModel as it already has vacation repository access, 
    // or I should create a separate one. Let's create a quick lazy one for now.
    private val viewModel: BillViewModel by viewModels()
    private lateinit var voiceInputHelper: VoiceInputHelper

    private var customerId: Int = -1
    private var customerName: String = ""
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vacation)

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
        toolbar.title = "Vacations: $customerName"

        voiceInputHelper = VoiceInputHelper(this)

        val rvVacations = findViewById<RecyclerView>(R.id.rvVacations)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddVacation)

        val adapter = VacationAdapter(
            onEdit = { vacation -> showVacationDialog(vacation) },
            onDelete = { vacation -> showDeleteConfirmation(vacation) }
        )
        rvVacations.adapter = adapter
        rvVacations.layoutManager = LinearLayoutManager(this)

        viewModel.getVacations(customerId).observe(this) { vacations ->
            adapter.setVacations(vacations)
        }

        fabAdd.setOnClickListener { showVacationDialog(null) }
    }

    private fun showVacationDialog(existingVacation: Vacation?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_vacation, null)
        val etStart = dialogView.findViewById<EditText>(R.id.etStartDate)
        val etEnd = dialogView.findViewById<EditText>(R.id.etEndDate)
        val etRemarks = dialogView.findViewById<EditText>(R.id.etRemarks)
        
        voiceInputHelper.attachTo(dialogView.findViewById(R.id.tilRemarks))

        if (existingVacation != null) {
            etStart.setText(existingVacation.startDate)
            etEnd.setText(existingVacation.endDate)
            etRemarks.setText(existingVacation.remarks)
        }

        etStart.setOnClickListener { showDatePicker { date -> etStart.setText(date) } }
        etEnd.setOnClickListener { showDatePicker { date -> etEnd.setText(date) } }

        AlertDialog.Builder(this)
            .setTitle(if (existingVacation == null) "Add Vacation" else "Edit Vacation")
            .setView(dialogView)
            .setPositiveButton("SAVE") { _, _ ->
                val start = etStart.text.toString()
                val end = etEnd.text.toString()
                val remarks = etRemarks.text.toString()

                if (start.isNotEmpty() && end.isNotEmpty()) {
                    val vacation = Vacation(
                        id = existingVacation?.id ?: 0,
                        customerId = customerId,
                        startDate = start,
                        endDate = end,
                        remarks = remarks
                    )
                    if (existingVacation == null) viewModel.insertVacation(vacation) else viewModel.updateVacation(vacation)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val picker = MaterialDatePicker.Builder.datePicker().build()
        picker.addOnPositiveButtonClickListener { selection ->
            onDateSelected(dateFormatter.format(Date(selection)))
        }
        picker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun showDeleteConfirmation(vacation: Vacation) {
        AlertDialog.Builder(this)
            .setTitle("Delete Vacation?")
            .setMessage("Are you sure you want to delete this pause?")
            .setPositiveButton("YES") { _, _ -> viewModel.deleteVacation(vacation) }
            .setNegativeButton("NO", null)
            .show()
    }

    override fun onDestroy() {
        voiceInputHelper.destroy()
        super.onDestroy()
    }
}
