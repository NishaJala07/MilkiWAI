package com.example.milkiwai.ui.billing

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.data.entity.BillWithCustomer
import java.text.DateFormatSymbols

class BillAdapter(
    private val onClick: (BillWithCustomer) -> Unit,
    private val onPayClick: (BillWithCustomer) -> Unit
) : RecyclerView.Adapter<BillAdapter.BillViewHolder>() {

    private var bills = emptyList<BillWithCustomer>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bill, parent, false)
        return BillViewHolder(view)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        holder.bind(bills[position])
    }

    override fun getItemCount() = bills.size

    fun setBills(newBills: List<BillWithCustomer>) {
        bills = newBills
        notifyDataSetChanged()
    }

    inner class BillViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        private val tvBillPeriod: TextView = itemView.findViewById(R.id.tvBillPeriod)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val btnDigitalPay: View = itemView.findViewById(R.id.btnDigitalPay)

        fun bind(item: BillWithCustomer) {
            val bill = item.bill
            tvCustomerName.text = item.customer?.name ?: "Unknown Customer"
            val monthName = DateFormatSymbols().months[bill.month - 1]
            tvBillPeriod.text = "$monthName ${bill.year}"
            tvAmount.text = "₹${bill.totalAmount}"
            tvStatus.text = bill.paymentStatus
            
            if (bill.paymentStatus == "PAID") {
                tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                btnDigitalPay.visibility = View.GONE
            } else {
                tvStatus.setTextColor(Color.parseColor("#F44336"))
                btnDigitalPay.visibility = View.VISIBLE
            }

            itemView.setOnClickListener { onClick(item) }
            btnDigitalPay.setOnClickListener { onPayClick(item) }
        }
    }
}
