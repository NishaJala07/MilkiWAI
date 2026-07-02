package com.example.milkiwai.ui.brand

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.data.entity.MilkBrand

class BrandAdapter(
    private val onEdit: (MilkBrand) -> Unit,
    private val onDelete: (MilkBrand) -> Unit
) : ListAdapter<MilkBrand, BrandAdapter.BrandViewHolder>(BrandDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_brand, parent, false)
        return BrandViewHolder(view)
    }

    override fun onBindViewHolder(holder: BrandViewHolder, position: Int) {
        val brand = getItem(position)
        holder.bind(brand)
    }

    inner class BrandViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvBrandName: TextView = itemView.findViewById(R.id.tvBrandName)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(brand: MilkBrand) {
            tvBrandName.text = brand.brandName
            tvPrice.text = "₹${brand.pricePerLitre} / Litre"
            btnEdit.setOnClickListener { onEdit(brand) }
            btnDelete.setOnClickListener { onDelete(brand) }
        }
    }

    class BrandDiffCallback : DiffUtil.ItemCallback<MilkBrand>() {
        override fun areItemsTheSame(oldItem: MilkBrand, newItem: MilkBrand): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MilkBrand, newItem: MilkBrand): Boolean {
            return oldItem == newItem
        }
    }
}
