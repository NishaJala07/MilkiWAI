package com.example.milkiwai.ui.reports

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.utils.BillingCalculator

class ProcurementAdapter : RecyclerView.Adapter<ProcurementAdapter.ProcurementViewHolder>() {

    private var items = emptyList<BillingCalculator.ProcurementItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProcurementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_procurement, parent, false)
        return ProcurementViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProcurementViewHolder, position: Int) {
        val item = items[position]
        holder.tvBrandName.text = item.brandName
        holder.tvQuantity.text = "${item.quantity} L"
    }

    override fun getItemCount() = items.size

    fun setItems(newItems: List<BillingCalculator.ProcurementItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ProcurementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBrandName: TextView = itemView.findViewById(R.id.tvBrandName)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
    }
}
