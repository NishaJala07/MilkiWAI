package com.example.milkiwai.ui.register

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.data.entity.SubscriptionWithBrand
import com.example.milkiwai.data.entity.DailyTransaction
import java.util.Locale

class RegisterAdapter : RecyclerView.Adapter<RegisterAdapter.RegisterViewHolder>() {

    private var subscriptions = emptyList<SubscriptionWithBrand>()
    private var transactions = emptyList<DailyTransaction>()
    private var daysInMonth = 30
    private var currentMonthYear = "" // e.g. "Jun 2026"

    fun setSubscriptions(subscriptions: List<SubscriptionWithBrand>) {
        this.subscriptions = subscriptions
        notifyDataSetChanged()
    }

    fun setTransactions(transactions: List<DailyTransaction>, monthYear: String) {
        this.transactions = transactions
        this.currentMonthYear = monthYear
        notifyDataSetChanged()
    }

    fun setDaysInMonth(days: Int) {
        this.daysInMonth = days
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegisterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_register_row, parent, false)
        return RegisterViewHolder(view)
    }

    override fun onBindViewHolder(holder: RegisterViewHolder, position: Int) {
        val sub = subscriptions[position]
        val brandId = sub.brand?.id ?: -1
        holder.bind(sub, transactions.filter { it.customerId == sub.subscription.customerId && it.brandId == brandId })
    }

    override fun getItemCount(): Int = subscriptions.size

    inner class RegisterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvCustomerName)
        private val tvBrand: TextView = itemView.findViewById(R.id.tvBrandName)
        private val layoutDays: LinearLayout = itemView.findViewById(R.id.layoutDays)
        private val tvTotal: TextView = itemView.findViewById(R.id.tvTotalQty)

        fun bind(sub: SubscriptionWithBrand, customerTransactions: List<DailyTransaction>) {
            tvName.text = sub.customer?.name ?: "Unknown Customer"
            tvBrand.text = sub.brand?.brandName ?: "Unknown Brand"
            layoutDays.removeAllViews()

            var totalMonth = 0.0
            for (day in 1..daysInMonth) {
                val dayStr = if (day < 10) "0$day" else day.toString()
                val fullDateStr = "$dayStr $currentMonthYear"
                
                val dailyQty = customerTransactions.find { 
                    it.transactionDate == fullDateStr
                }?.quantity ?: 0.0
                
                totalMonth += dailyQty

                val tv = TextView(itemView.context).apply {
                    layoutParams = LinearLayout.LayoutParams(40.dpToPx(), LinearLayout.LayoutParams.WRAP_CONTENT)
                    text = if (dailyQty > 0) String.format(Locale.getDefault(), "%.1f", dailyQty) else "-"
                    gravity = Gravity.CENTER
                    textSize = 12f
                    setPadding(0, 4.dpToPx(), 0, 4.dpToPx())
                    setTextColor(if (dailyQty > 0) context.getColor(R.color.text_main) else context.getColor(R.color.text_secondary))
                }
                layoutDays.addView(tv)
            }

            tvTotal.text = String.format("%.1f L", totalMonth)
        }

        private fun Int.dpToPx(): Int = (this * itemView.resources.displayMetrics.density).toInt()
    }
}
