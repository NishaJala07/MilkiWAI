package com.example.milkiwai.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.data.entity.BillWithCustomer
import com.example.milkiwai.viewmodel.BillViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.net.URLEncoder

class PaymentReminderActivity : AppCompatActivity() {

    private val viewModel: BillViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_reminder)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val rvPendingBills = findViewById<RecyclerView>(R.id.rvPendingBills)
        val llNoRecords = findViewById<android.view.View>(R.id.llNoRecords)
        
        val adapter = ReminderAdapter(
            onSms = { bill -> sendSmsReminder(bill) },
            onWhatsApp = { bill -> sendWhatsAppReminder(bill) }
        )
        rvPendingBills.adapter = adapter

        viewModel.allBills.observe(this) { bills ->
            val pending = bills.filter { it.bill.paymentStatus == "PENDING" }
            adapter.setBills(pending)
            
            if (pending.isEmpty()) {
                llNoRecords.visibility = android.view.View.VISIBLE
                rvPendingBills.visibility = android.view.View.GONE
            } else {
                llNoRecords.visibility = android.view.View.GONE
                rvPendingBills.visibility = android.view.View.VISIBLE
            }
        }
    }

    private fun sendSmsReminder(billWithCustomer: BillWithCustomer) {
        val customer = billWithCustomer.customer ?: return
        val bill = billWithCustomer.bill
        val message = "Dear ${customer.name}, Your milk bill of ₹${bill.totalAmount} is pending. Please make payment. Thank You."
        
        try {
            val smsManager = if (android.os.Build.VERSION.SDK_INT >= 31) {
                this.getSystemService(android.telephony.SmsManager::class.java)
            } else {
                android.telephony.SmsManager.getDefault()
            }
            smsManager.sendTextMessage(customer.mobile, null, message, null, null)
            Toast.makeText(this, "SMS Reminder Sent to ${customer.name}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error sending SMS: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun sendWhatsAppReminder(billWithCustomer: BillWithCustomer) {
        val customer = billWithCustomer.customer ?: return
        val bill = billWithCustomer.bill
        val message = "*Milk Bill Reminder*\nAmount Due: ₹${bill.totalAmount}\nThank You."
        
        try {
            val url = "https://api.whatsapp.com/send?phone=91${customer.mobile}&text=" + URLEncoder.encode(message, "UTF-8")
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "WhatsApp not installed or error occurred", Toast.LENGTH_SHORT).show()
        }
    }

    class ReminderAdapter(
        private val onSms: (BillWithCustomer) -> Unit,
        private val onWhatsApp: (BillWithCustomer) -> Unit
    ) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {
        private var bills = listOf<BillWithCustomer>()

        fun setBills(newBills: List<BillWithCustomer>) {
            bills = newBills
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_payment_reminder, parent, false)
            return ReminderViewHolder(view)
        }

        override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
            holder.bind(bills[position], onSms, onWhatsApp)
        }

        override fun getItemCount(): Int = bills.size

        class ReminderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val tvName: TextView = view.findViewById(R.id.tvCustomerName)
            private val tvAmount: TextView = view.findViewById(R.id.tvBillAmount)
            private val btnSms: MaterialButton = view.findViewById(R.id.btnSms)
            private val btnWhatsApp: MaterialButton = view.findViewById(R.id.btnWhatsApp)

            fun bind(bill: BillWithCustomer, onSms: (BillWithCustomer) -> Unit, onWhatsApp: (BillWithCustomer) -> Unit) {
                tvName.text = bill.customer?.name ?: "Unknown Customer"
                tvAmount.text = "₹${bill.bill.totalAmount}"
                btnSms.setOnClickListener { onSms(bill) }
                btnWhatsApp.setOnClickListener { onWhatsApp(bill) }
                
                // Disable buttons if customer is null
                btnSms.isEnabled = bill.customer != null
                btnWhatsApp.isEnabled = bill.customer != null
            }
        }
    }
}
