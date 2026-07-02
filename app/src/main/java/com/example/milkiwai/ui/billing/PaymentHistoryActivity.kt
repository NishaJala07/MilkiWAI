package com.example.milkiwai.ui.billing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.data.entity.BillWithCustomer
import com.example.milkiwai.viewmodel.BillViewModel
import com.google.android.material.appbar.MaterialToolbar
import java.text.DateFormatSymbols

class PaymentHistoryActivity : AppCompatActivity() {

    private val viewModel: BillViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_history)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val rvPayments = findViewById<RecyclerView>(R.id.rvPayments)
        val adapter = PaymentHistoryAdapter()
        rvPayments.adapter = adapter

        viewModel.allBills.observe(this) { bills ->
            val paidBills = bills.filter { it.bill.paymentStatus == "PAID" }
                .sortedByDescending { it.bill.paymentDate }
            adapter.setBills(paidBills)
        }
    }

    class PaymentHistoryAdapter : RecyclerView.Adapter<PaymentHistoryAdapter.HistoryViewHolder>() {
        private var bills = listOf<BillWithCustomer>()

        fun setBills(newBills: List<BillWithCustomer>) {
            bills = newBills
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_payment_history, parent, false)
            return HistoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
            holder.bind(bills[position])
        }

        override fun getItemCount(): Int = bills.size

        class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val tvName: TextView = view.findViewById(R.id.tvCustomerName)
            private val tvAmount: TextView = view.findViewById(R.id.tvAmount)
            private val tvBillInfo: TextView = view.findViewById(R.id.tvBillInfo)
            private val tvDate: TextView = view.findViewById(R.id.tvPaymentDate)
            private val tvMode: TextView = view.findViewById(R.id.tvPaymentMode)
            private val tvTxnId: TextView = view.findViewById(R.id.tvTransactionId)

            fun bind(item: BillWithCustomer) {
                val bill = item.bill
                tvName.text = item.customer?.name ?: "Unknown Customer"
                tvAmount.text = "₹${bill.totalAmount}"
                val monthName = DateFormatSymbols().months[bill.month - 1]
                tvBillInfo.text = "$monthName ${bill.year} Bill"
                tvDate.text = "Date: ${bill.paymentDate}"
                tvMode.text = bill.paymentMode
                tvTxnId.text = "Txn ID: ${bill.transactionId}"
            }
        }
    }
}
