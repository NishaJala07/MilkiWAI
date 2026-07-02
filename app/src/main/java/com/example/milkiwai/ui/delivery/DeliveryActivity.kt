package com.example.milkiwai.ui.delivery

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.ui.procurement.DeliveryAdapter
import com.example.milkiwai.viewmodel.DeliveryViewModel
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.*

class DeliveryActivity : AppCompatActivity() {

    private val viewModel: DeliveryViewModel by viewModels()
    private lateinit var adapter: DeliveryAdapter
    private val currentDate = Date()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val tvDate = findViewById<TextView>(R.id.tvDate)
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        tvDate.text = getString(R.string.delivery_date_format, sdf.format(currentDate))

        val rvDeliveries = findViewById<RecyclerView>(R.id.rvDeliveries)
        adapter = DeliveryAdapter(
            onDelivered = { item ->
                viewModel.markAsDelivered(item, currentDate)
            }
        ) { item ->
            showMissedDialog(item)
        }
        rvDeliveries.adapter = adapter
        rvDeliveries.layoutManager = LinearLayoutManager(this)

        viewModel.deliveryItems.observe(this) { items ->
            adapter.setItems(items)
        }

        viewModel.loadDeliveries(currentDate)
        
        findViewById<android.widget.Button>(R.id.btnViewSummary).setOnClickListener {
            startActivity(android.content.Intent(this, DeliverySummaryActivity::class.java))
        }
    }

    private fun showMissedDialog(item: com.example.milkiwai.data.model.DeliveryItem) {
        val input = EditText(this)
        input.hint = getString(R.string.reason_optional)
        
        AlertDialog.Builder(this)
            .setTitle(R.string.mark_as_missed)
            .setMessage(getString(R.string.missed_confirmation_msg, item.customer.name))
            .setView(input)
            .setPositiveButton(R.string.missed) { _, _ ->
                viewModel.markAsMissed(item, currentDate, input.text.toString())
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
