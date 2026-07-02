package com.example.milkiwai.ui.reports

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.viewmodel.ReportViewModel
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.flow.collectLatest

class ProcurementReportActivity : AppCompatActivity() {

    private val viewModel: ReportViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_procurement_report)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val rvProcurement = findViewById<RecyclerView>(R.id.rvProcurement)
        val tvTotalQty = findViewById<TextView>(R.id.tvTotalQty)
        val tvSuggestedQty = findViewById<TextView>(R.id.tvSuggestedQty)

        val adapter = ProcurementAdapter()
        rvProcurement.adapter = adapter
        rvProcurement.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launchWhenStarted {
            viewModel.procurementResult.collectLatest { items ->
                adapter.setItems(items)
                val totalQty = items.sumOf { it.quantity }
                tvTotalQty.text = "Required: $totalQty L"
                
                val suggestedQty = totalQty * 1.05 // 5% Buffer
                tvSuggestedQty.text = "Suggested (inc. 5% buffer): ${String.format("%.2f", suggestedQty)} L"
            }
        }
        
        viewModel.calculateTomorrowProcurement()
    }
}
