package com.example.milkiwai.ui.reports

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.milkiwai.R
import com.example.milkiwai.viewmodel.ReportViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.flow.collectLatest
import java.util.*

import com.example.milkiwai.utils.pdf.PdfGenerator
import androidx.core.content.FileProvider
import java.io.File

class ReportsActivity : AppCompatActivity() {

    private val viewModel: ReportViewModel by viewModels()
    private lateinit var pdfGenerator: PdfGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        pdfGenerator = PdfGenerator(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val tvTodayRevenue = findViewById<TextView>(R.id.tvTodayRevenue)
        val tvMonthlyRevenue = findViewById<TextView>(R.id.tvMonthlyRevenue)
        val tvPendingAmount = findViewById<TextView>(R.id.tvPendingAmount)
        val tvMilkRequired = findViewById<TextView>(R.id.tvMilkRequired)
        val tvTopBrand = findViewById<TextView>(R.id.tvTopBrand)
        val tvTopCustomer = findViewById<TextView>(R.id.tvTopCustomer)

        viewModel.getTodayRevenue().observe(this) { revenue ->
            tvTodayRevenue.text = "₹${(revenue ?: 0.0).toInt()}"
        }

        val calendar = Calendar.getInstance()
        viewModel.getMonthlyRevenue(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)).observe(this) { revenue ->
            tvMonthlyRevenue.text = "₹${(revenue ?: 0.0).toInt()}"
        }

        viewModel.getPendingPayments().observe(this) { pendingBills ->
            val totalPending = pendingBills.sumOf { it.bill.totalAmount }
            tvPendingAmount.text = "₹${totalPending.toInt()}"
            setupPieChart(totalPending)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.procurementResult.collectLatest { items ->
                val totalQty = items.sumOf { it.quantity }
                tvMilkRequired.text = "$totalQty L Required for Tomorrow"
                setupForecastChart(items)
            }
        }
        viewModel.calculateTomorrowProcurement()

        viewModel.getBrandWiseSales().observe(this) { sales ->
            setupRevenueChart(sales)
            if (sales.isNotEmpty()) {
                val topSale = sales.maxByOrNull { it.totalQuantity }
                viewModel.getAllBrands().observe(this) { brands ->
                    val brand = brands.find { it.id == topSale?.brandId }
                    tvTopBrand.text = brand?.brandName ?: "Brand ${topSale?.brandId}"
                }
            }
        }

        viewModel.getCustomerWiseRevenue().observe(this) { revenueList ->
            if (revenueList.isNotEmpty()) {
                val top = revenueList.first()
                viewModel.getAllCustomers().observe(this) { customers ->
                    val customer = customers.find { it.id == top.customerId }
                    tvTopCustomer.text = customer?.name ?: "Customer ${top.customerId}"
                    setupCustomerChart(revenueList.take(5), customers)
                }
            }
        }

        findViewById<Button>(R.id.btnExportPdf).setOnClickListener {
            exportAnalyticsReport()
        }

        findViewById<Button>(R.id.btnOcrRequests).setOnClickListener {
            startActivity(Intent(this, com.example.milkiwai.ui.RequestParserActivity::class.java))
        }
    }

    private fun setupRevenueChart(sales: List<com.example.milkiwai.data.dao.BrandSales>) {
        val barChart = findViewById<BarChart>(R.id.revenueChart)
        val entries = sales.mapIndexed { index, brandSales ->
            BarEntry(index.toFloat(), brandSales.totalQuantity.toFloat())
        }

        val dataSet = BarDataSet(entries, "Brand Sales (L)")
        dataSet.colors = listOf(Color.parseColor("#1565C0"), Color.parseColor("#42A5F5"), Color.parseColor("#26A69A"))
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 10f

        val barData = BarData(dataSet)
        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.setDrawGridLines(false)
        barChart.axisLeft.setDrawGridLines(true)
        barChart.axisRight.isEnabled = false
        barChart.animateY(1000)
        barChart.invalidate()
    }

    private fun setupForecastChart(items: List<com.example.milkiwai.utils.BillingCalculator.ProcurementItem>) {
        val chart = findViewById<HorizontalBarChart>(R.id.forecastChart)
        val entries = items.mapIndexed { index, item ->
            BarEntry(index.toFloat(), item.quantity.toFloat())
        }

        val dataSet = BarDataSet(entries, "Tomorrow's Demand (L)")
        dataSet.color = Color.parseColor("#26A69A")
        
        val barData = BarData(dataSet)
        chart.data = barData
        chart.description.isEnabled = false
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(items.map { it.brandName })
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setDrawGridLines(false)
        chart.axisLeft.isEnabled = false
        chart.animateY(1000)
        chart.invalidate()
    }

    private fun setupCustomerChart(revenueList: List<com.example.milkiwai.data.dao.CustomerRevenue>, customers: List<com.example.milkiwai.data.entity.Customer>) {
        val chart = findViewById<HorizontalBarChart>(R.id.customerChart)
        val entries = revenueList.mapIndexed { index, rev ->
            BarEntry(index.toFloat(), rev.totalRevenue.toFloat())
        }

        val dataSet = BarDataSet(entries, "Revenue Contribution (₹)")
        dataSet.color = Color.parseColor("#FF9800")
        
        val names = revenueList.map { rev ->
            customers.find { it.id == rev.customerId }?.name ?: "ID: ${rev.customerId}"
        }

        chart.data = BarData(dataSet)
        chart.description.isEnabled = false
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(names)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setDrawGridLines(false)
        chart.animateY(1000)
        chart.invalidate()
    }

    private fun setupPieChart(pendingAmount: Double) {
        val pieChart = findViewById<PieChart>(R.id.paymentPieChart)
        val entries = mutableListOf<PieEntry>()
        entries.add(PieEntry(pendingAmount.toFloat(), "Pending"))
        // Placeholder for collected - in real app fetch PAID total
        entries.add(PieEntry((pendingAmount * 0.7).toFloat(), "Collected"))
        
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(Color.parseColor("#E53935"), Color.parseColor("#43A047"))
        dataSet.sliceSpace = 3f
        
        val pieData = PieData(dataSet)
        pieData.setValueFormatter(PercentFormatter(pieChart))
        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 40f
        pieChart.setUsePercentValues(true)
        pieChart.animateXY(1000, 1000)
        pieChart.invalidate()
    }

    private fun exportAnalyticsReport() {
        // Implementation for exporting PDF
        Toast.makeText(this, "Generating Analytics Report...", Toast.LENGTH_SHORT).show()
    }
}
