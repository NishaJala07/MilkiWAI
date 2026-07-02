package com.example.milkiwai.ui.customer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.data.entity.QuantityChangeWithBrand

class QuantityChangeAdapter(
    private val onEdit: (QuantityChangeWithBrand) -> Unit,
    private val onDelete: (QuantityChangeWithBrand) -> Unit
) : RecyclerView.Adapter<QuantityChangeAdapter.ChangeViewHolder>() {

    private var changes = emptyList<QuantityChangeWithBrand>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChangeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quantity_change, parent, false)
        return ChangeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChangeViewHolder, position: Int) {
        val item = changes[position]
        holder.bind(item)
    }

    override fun getItemCount() = changes.size

    fun setChanges(newChanges: List<QuantityChangeWithBrand>) {
        changes = newChanges
        notifyDataSetChanged()
    }

    inner class ChangeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvBrandName: TextView = itemView.findViewById(R.id.tvBrandName)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvQuantityFlow: TextView = itemView.findViewById(R.id.tvQuantityFlow)
        private val tvDateRange: TextView = itemView.findViewById(R.id.tvDateRange)
        private val tvRemarks: TextView = itemView.findViewById(R.id.tvRemarks)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(item: QuantityChangeWithBrand) {
            val change = item.quantityChange
            tvBrandName.text = item.brand?.brandName ?: "Unknown Brand"
            tvStatus.text = change.status
            
            val statusColor = when(change.status) {
                "ACTIVE" -> "#4CAF50"
                "COMPLETED" -> "#2196F3"
                "CANCELLED" -> "#F44336"
                else -> "#757575"
            }
            tvStatus.background.setTint(android.graphics.Color.parseColor(statusColor))

            tvQuantityFlow.text = "${change.oldQuantity}L → ${change.newQuantity}L"
            tvDateRange.text = "${change.startDate} - ${change.endDate}"
            tvRemarks.text = change.remarks
            tvRemarks.visibility = if (change.remarks.isEmpty()) View.GONE else View.VISIBLE
            
            btnEdit.setOnClickListener { onEdit(item) }
            btnDelete.setOnClickListener { onDelete(item) }
        }
    }
}
