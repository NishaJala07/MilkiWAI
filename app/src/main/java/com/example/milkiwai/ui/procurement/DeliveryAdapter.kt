package com.example.milkiwai.ui.procurement

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.data.model.DeliveryItem
import com.google.android.material.button.MaterialButton

class DeliveryAdapter(
    private val onDelivered: (DeliveryItem) -> Unit,
    private val onMissed: (DeliveryItem) -> Unit
) : RecyclerView.Adapter<DeliveryAdapter.DeliveryViewHolder>() {

    private var items = listOf<DeliveryItem>()

    fun setItems(newItems: List<DeliveryItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_delivery, parent, false)
        return DeliveryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeliveryViewHolder, position: Int) {
        holder.bind(items[position], onDelivered, onMissed)
    }

    override fun getItemCount(): Int = items.size

    class DeliveryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvCustomerName: TextView = view.findViewById(R.id.tvCustomerName)
        private val tvBrandAndQty: TextView = view.findViewById(R.id.tvBrandAndQty)
        private val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        private val btnDelivered: MaterialButton = view.findViewById(R.id.btnDelivered)
        private val btnMissed: MaterialButton = view.findViewById(R.id.btnMissed)
        private val layoutActions: View = view.findViewById(R.id.layoutActions)
        private val tvRemarks: TextView = view.findViewById(R.id.tvRemarks)

        fun bind(item: DeliveryItem, onDelivered: (DeliveryItem) -> Unit, onMissed: (DeliveryItem) -> Unit) {
            tvCustomerName.text = item.customer.name
            tvBrandAndQty.text = "${item.brand.brandName} - ${item.scheduledQuantity} L"
            
            val delivery = item.delivery
            if (delivery == null) {
                tvStatus.text = "PENDING"
                tvStatus.setBackgroundResource(R.drawable.status_bg) // Should be grey/yellow
                layoutActions.visibility = View.VISIBLE
                tvRemarks.visibility = View.GONE
            } else {
                tvStatus.text = delivery.status
                layoutActions.visibility = View.GONE
                
                when (delivery.status) {
                    "DELIVERED" -> {
                        tvStatus.setTextColor(Color.WHITE)
                        // In a real app we'd set background color programmatically or via state list
                    }
                    "MISSED" -> {
                        tvStatus.setTextColor(Color.RED)
                    }
                }
                
                if (delivery.remarks.isNotEmpty()) {
                    tvRemarks.visibility = View.VISIBLE
                    tvRemarks.text = "Remarks: ${delivery.remarks}"
                } else {
                    tvRemarks.visibility = View.GONE
                }
            }

            btnDelivered.setOnClickListener { onDelivered(item) }
            btnMissed.setOnClickListener { onMissed(item) }
        }
    }
}
