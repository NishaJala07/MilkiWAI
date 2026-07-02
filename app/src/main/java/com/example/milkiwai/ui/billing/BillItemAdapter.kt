package com.example.milkiwai.ui.billing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.data.entity.BillItemWithBrand

class BillItemAdapter : RecyclerView.Adapter<BillItemAdapter.ItemViewHolder>() {

    private var items = emptyList<BillItemWithBrand>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bill_detail, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun setItems(newItems: List<BillItemWithBrand>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvBrandName: TextView = itemView.findViewById(R.id.tvBrandName)
        private val tvQty: TextView = itemView.findViewById(R.id.tvQty)
        private val tvRate: TextView = itemView.findViewById(R.id.tvRate)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)

        fun bind(item: BillItemWithBrand) {
            val bi = item.billItem
            tvBrandName.text = item.brand?.brandName ?: "Unknown Brand"
            tvQty.text = "${bi.totalQuantity} L"
            tvRate.text = "₹${bi.ratePerLitre}/L"
            tvAmount.text = "₹${bi.amount}"
        }
    }
}
