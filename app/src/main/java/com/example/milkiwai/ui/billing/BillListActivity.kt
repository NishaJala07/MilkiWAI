package com.example.milkiwai.ui.billing

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.viewmodel.BillViewModel
import com.google.android.material.appbar.MaterialToolbar

class BillListActivity : AppCompatActivity() {

    private val viewModel: BillViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_list)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val rvBills = findViewById<RecyclerView>(R.id.rvBills)
        val llNoBills = findViewById<android.view.View>(R.id.llNoBills)
        val adapter = BillAdapter(
            onClick = { item ->
                val intent = Intent(this, BillDetailsActivity::class.java).apply {
                    putExtra("BILL_ID", item.bill.id)
                    putExtra("CUSTOMER_NAME", item.customer?.name ?: "Unknown")
                    putExtra("MONTH", item.bill.month)
                    putExtra("YEAR", item.bill.year)
                    putExtra("TOTAL", item.bill.totalAmount)
                    putExtra("STATUS", item.bill.paymentStatus)
                    putExtra("PDF_PATH", item.bill.pdfPath)
                    putExtra("CUSTOMER_MOBILE", item.customer?.mobile ?: "")
                }
                startActivity(intent)
            },
            onPayClick = { item ->
                val monthName = java.text.DateFormatSymbols().months[item.bill.month - 1]
                val intent = Intent(this, PaymentActivity::class.java).apply {
                    putExtra("BILL_ID", item.bill.id)
                    putExtra("AMOUNT", item.bill.totalAmount)
                    putExtra("CUSTOMER_NAME", item.customer?.name ?: "Unknown")
                    putExtra("MONTH_YEAR", "$monthName ${item.bill.year}")
                }
                startActivity(intent)
            }
        )

        rvBills.adapter = adapter
        rvBills.layoutManager = LinearLayoutManager(this)

        viewModel.allBills.observe(this) { bills ->
            adapter.setBills(bills)
            llNoBills.visibility = if (bills.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            rvBills.visibility = if (bills.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
        }
    }
}
