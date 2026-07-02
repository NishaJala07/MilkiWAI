package com.example.milkiwai.ui.customer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.data.entity.SubscriptionWithBrand

class SubscriptionAdapter(
    private val onEdit: (SubscriptionWithBrand) -> Unit,
    private val onDelete: (SubscriptionWithBrand) -> Unit
) : RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder>() {

    private var subscriptions = emptyList<SubscriptionWithBrand>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subscription, parent, false)
        return SubscriptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int) {
        val item = subscriptions[position]
        holder.bind(item)
    }

    override fun getItemCount() = subscriptions.size

    fun setSubscriptions(newSubscriptions: List<SubscriptionWithBrand>) {
        subscriptions = newSubscriptions
        notifyDataSetChanged()
    }

    inner class SubscriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvBrandName: TextView = itemView.findViewById(R.id.tvBrandName)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(item: SubscriptionWithBrand) {
            tvBrandName.text = item.brand?.brandName ?: "Unknown Brand"
            val unit = if (item.subscription.quantity > 1) "Litres" else "Litre"
            tvQuantity.text = "${item.subscription.quantity} $unit"
            btnEdit.setOnClickListener { onEdit(item) }
            btnDelete.setOnClickListener { onDelete(item) }
        }
    }
}
