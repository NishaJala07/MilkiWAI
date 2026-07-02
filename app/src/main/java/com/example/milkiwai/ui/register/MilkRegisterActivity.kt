package com.example.milkiwai.ui.register

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.data.entity.Customer
import com.example.milkiwai.data.entity.DailyTransaction
import com.example.milkiwai.viewmodel.CustomerViewModel
import com.example.milkiwai.viewmodel.DailyTransactionViewModel
import com.example.milkiwai.viewmodel.SubscriptionViewModel
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.*

class MilkRegisterActivity : AppCompatActivity() {

    private val customerViewModel: CustomerViewModel by viewModels()
    private val transactionViewModel: DailyTransactionViewModel by viewModels()
    private val subscriptionViewModel: SubscriptionViewModel by viewModels()
    private lateinit var adapter: RegisterAdapter
    private var currentCalendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_milk_register)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val rvRegister = findViewById<RecyclerView>(R.id.rvRegister)
        adapter = RegisterAdapter()
        rvRegister.adapter = adapter
        rvRegister.layoutManager = LinearLayoutManager(this)

        updateMonthDisplay()
        setupHeader()

        subscriptionViewModel.allSubscriptions.observe(this) { subscriptions ->
            adapter.setSubscriptions(subscriptions)
            loadTransactions()
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSelectMonth).setOnClickListener {
            // Simple month selector logic could go here
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnGenerateBills).setOnClickListener {
            startActivity(Intent(this, com.example.milkiwai.ui.billing.GenerateBillActivity::class.java))
        }

        findViewById<com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton>(R.id.btnHistoricalEntry).setOnClickListener {
            startActivity(Intent(this, HistoricalEntryActivity::class.java))
        }
    }

    private fun setupHeader() {
        val layoutHeader = findViewById<LinearLayout>(R.id.layoutDaysHeader)
        layoutHeader.removeAllViews()
        
        val daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..daysInMonth) {
            val tv = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(40.dpToPx(), LinearLayout.LayoutParams.WRAP_CONTENT)
                text = i.toString()
                gravity = Gravity.CENTER
                textSize = 12f
                setTextColor(getColor(R.color.text_secondary))
            }
            layoutHeader.addView(tv)
        }
        adapter.setDaysInMonth(daysInMonth)
    }

    private fun loadTransactions() {
        val monthYear = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(currentCalendar.time)
        transactionViewModel.getTransactionsByMonth(monthYear).observe(this) { transactions ->
            adapter.setTransactions(transactions, monthYear)
        }
    }

    private fun updateMonthDisplay() {
        val monthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentCalendar.time)
        findViewById<TextView>(R.id.tvMonthYear).text = monthYear
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
    private fun Int.spToPx(): Float = this * resources.displayMetrics.scaledDensity
}
