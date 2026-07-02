package com.example.milkiwai.ui.customer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.data.entity.Customer
import com.google.android.material.button.MaterialButton

class CustomerAdapter(
    private val onViewClick: (Customer) -> Unit,
    private val onEditClick: (Customer) -> Unit,
    private val onDeleteClick: (Customer) -> Unit
) : RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder>() {

    private var customers = emptyList<Customer>()

    class CustomerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvBuildingInfo: TextView = itemView.findViewById(R.id.tvBuildingInfo)
        val tvRoomInfo: TextView = itemView.findViewById(R.id.tvRoomInfo)
        val tagActiveSub: TextView = itemView.findViewById(R.id.tagActiveSub)
        val tagPendingPayment: TextView = itemView.findViewById(R.id.tagPendingPayment)
        val btnView: MaterialButton = itemView.findViewById(R.id.btnView)
        val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_customer, parent, false)
        return CustomerViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val current = customers[position]
        holder.tvName.text = current.name
        holder.tvBuildingInfo.text = current.buildingName
        holder.tvRoomInfo.text = "Room: ${current.roomNo}"
        
        // Default tags (can be improved by passing status from activity/viewmodel)
        holder.tagActiveSub.visibility = View.VISIBLE
        holder.tagPendingPayment.visibility = View.GONE

        holder.btnView.setOnClickListener { onViewClick(current) }
        holder.btnEdit.setOnClickListener { onEditClick(current) }
        holder.btnDelete.setOnClickListener { onDeleteClick(current) }
        holder.itemView.setOnClickListener { onViewClick(current) }
    }

    override fun getItemCount() = customers.size

    fun setCustomers(customers: List<Customer>) {
        this.customers = customers
        notifyDataSetChanged()
    }
}
