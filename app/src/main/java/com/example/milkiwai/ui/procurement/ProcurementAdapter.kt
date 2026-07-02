package com.example.milkiwai.ui.procurement

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.data.model.ProcurementReport

class ProcurementAdapter : RecyclerView.Adapter<ProcurementAdapter.ProcurementViewHolder>() {

    private var items = listOf<ProcurementReport>()

    fun setItems(newItems: List<ProcurementReport>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProcurementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_procurement, parent, false)
        return ProcurementViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProcurementViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ProcurementViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvBrandName: TextView = view.findViewById(R.id.tvBrandName)
        private val tvRequiredQty: TextView = view.findViewById(R.id.tvRequiredQty)
        private val tvSuggestedQty: TextView = view.findViewById(R.id.tvSuggestedQty)

        fun bind(report: ProcurementReport) {
            tvBrandName.text = report.brandName
            tvRequiredQty.text = "Required: ${report.requiredQuantity} L"
            tvSuggestedQty.text = "Suggested: ${String.format("%.2f", report.suggestedQuantity)} L"
        }
    }
}
