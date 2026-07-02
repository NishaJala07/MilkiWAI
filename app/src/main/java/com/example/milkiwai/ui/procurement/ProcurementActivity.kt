package com.example.milkiwai.ui.procurement

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.utils.pdf.PdfGenerator
import com.example.milkiwai.viewmodel.procurement.ProcurementViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ProcurementActivity : AppCompatActivity() {

    private val viewModel: ProcurementViewModel by viewModels()
    private lateinit var adapter: ProcurementAdapter
    private lateinit var pdfGenerator: PdfGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_procurement)

        pdfGenerator = PdfGenerator(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val rvItems = findViewById<RecyclerView>(R.id.rvProcurementItems)
        val tvTotalRequired = findViewById<TextView>(R.id.tvTotalRequired)
        val tvExpectedRevenue = findViewById<TextView>(R.id.tvExpectedRevenue)
        val tvTotalDeliveries = findViewById<TextView>(R.id.tvTotalDeliveries)
        val tvAverageDemand = findViewById<TextView>(R.id.tvAverageDemand)
        val btnWhatsApp = findViewById<MaterialButton>(R.id.btnShareWhatsApp)
        val btnPdf = findViewById<MaterialButton>(R.id.btnDownloadPdf)

        adapter = ProcurementAdapter()
        rvItems.adapter = adapter
        rvItems.layoutManager = LinearLayoutManager(this)

        viewModel.procurementReport.observe(this) { reports ->
            adapter.setItems(reports)
            val total = reports.sumOf { it.requiredQuantity }
            tvTotalRequired.text = "$total L"
            
            checkDemandAlert(total)
        }

        viewModel.expectedRevenue.observe(this) { revenue ->
            tvExpectedRevenue.text = "Expected Revenue: ₹$revenue"
        }

        viewModel.totalDeliveries.observe(this) { count ->
            tvTotalDeliveries.text = "Expected Deliveries: $count"
        }

        viewModel.averageDemand.observe(this) { avg ->
            tvAverageDemand.text = "30-Day Average: ${String.format("%.2f", avg)} L"
        }

        btnWhatsApp.setOnClickListener { shareViaWhatsApp() }
        btnPdf.setOnClickListener { generateAndOpenPdf() }

        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_YEAR, 1)
        viewModel.loadProcurementData(tomorrow.time)
    }

    private fun checkDemandAlert(currentDemand: Double) {
        val avg = viewModel.averageDemand.value ?: return
        val tvDemandAlert = findViewById<TextView>(R.id.tvDemandAlert)
        
        if (currentDemand < avg * 0.7) {
            tvDemandAlert.visibility = View.VISIBLE
            tvDemandAlert.text = "Alert: Demand dropped by ${String.format("%.0f", (1 - currentDemand/avg) * 100)}%"
        } else if (currentDemand > avg * 1.3) {
            tvDemandAlert.visibility = View.VISIBLE
            tvDemandAlert.text = "High Demand Expected Tomorrow!"
            // Use a default color or resource if available
            tvDemandAlert.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
        } else {
            tvDemandAlert.visibility = View.GONE
        }
    }

    private fun shareViaWhatsApp() {
        val reports = viewModel.procurementReport.value ?: return
        if (reports.isEmpty()) {
            Toast.makeText(this, "No data to share", Toast.LENGTH_SHORT).show()
            return
        }
        
        val sb = StringBuilder()
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_YEAR, 1)
        
        sb.append("Daily Procurement Report\n")
        sb.append("Date: ${sdf.format(tomorrow.time)}\n")
        sb.append("--------------------------------\n")
        
        reports.forEach { 
            sb.append("${it.brandName}\n")
            sb.append("Required: ${it.requiredQuantity}L\n")
            sb.append("Suggested: ${String.format("%.2f", it.suggestedQuantity)}L\n")
            sb.append("--------------------------------\n")
        }
        
        sb.append("Total: ${reports.sumOf { it.requiredQuantity }}L\n")

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, sb.toString())
        
        // Try to target WhatsApp if possible, but keep it flexible
        val whatsappIntent = Intent(Intent.ACTION_SEND)
        whatsappIntent.type = "text/plain"
        whatsappIntent.setPackage("com.whatsapp")
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, sb.toString())
        
        try {
            startActivity(whatsappIntent)
        } catch (e: Exception) {
            startActivity(Intent.createChooser(intent, "Share via"))
        }
    }

    private fun generateAndOpenPdf() {
        val reports = viewModel.procurementReport.value ?: return
        if (reports.isEmpty()) {
            Toast.makeText(this, "No data to generate PDF", Toast.LENGTH_SHORT).show()
            return
        }
        
        val headers = listOf("Brand", "Required (L)", "Suggested (L)")
        val rows = reports.map { 
            listOf(it.brandName, it.requiredQuantity.toString(), String.format("%.2f", it.suggestedQuantity))
        }
        
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_YEAR, 1)
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        
        val path = pdfGenerator.generateReportPdf(
            "Procurement Report - ${sdf.format(tomorrow.time)}",
            headers,
            rows,
            "Total Required: ${reports.sumOf { it.requiredQuantity }} L"
        )
        
        val file = File(path)
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(intent)
    }
}
