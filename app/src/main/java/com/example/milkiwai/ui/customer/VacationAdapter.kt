package com.example.milkiwai.ui.customer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.data.entity.Vacation

class VacationAdapter(
    private val onEdit: (Vacation) -> Unit,
    private val onDelete: (Vacation) -> Unit
) : RecyclerView.Adapter<VacationAdapter.VacationViewHolder>() {

    private var vacations = emptyList<Vacation>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VacationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_vacation, parent, false)
        return VacationViewHolder(view)
    }

    override fun onBindViewHolder(holder: VacationViewHolder, position: Int) {
        holder.bind(vacations[position])
    }

    override fun getItemCount() = vacations.size

    fun setVacations(newVacations: List<Vacation>) {
        vacations = newVacations
        notifyDataSetChanged()
    }

    inner class VacationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDateRange: TextView = itemView.findViewById(R.id.tvDateRange)
        private val tvRemarks: TextView = itemView.findViewById(R.id.tvRemarks)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(vacation: Vacation) {
            tvDateRange.text = "${vacation.startDate} - ${vacation.endDate}"
            tvRemarks.text = vacation.remarks
            tvRemarks.visibility = if (vacation.remarks.isEmpty()) View.GONE else View.VISIBLE
            
            btnEdit.setOnClickListener { onEdit(vacation) }
            btnDelete.setOnClickListener { onDelete(vacation) }
        }
    }
}
