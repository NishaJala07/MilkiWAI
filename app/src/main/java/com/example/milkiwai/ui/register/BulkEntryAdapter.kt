package com.example.milkiwai.ui.register

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.utils.VoiceInputHelper
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class BulkEntryAdapter(private val activity: AppCompatActivity) : RecyclerView.Adapter<BulkEntryAdapter.BulkViewHolder>() {

    private var items = emptyList<HistoricalEntryActivity.BulkEntryItem>()
    private val voiceInputHelper = VoiceInputHelper(activity)

    fun setItems(newItems: List<HistoricalEntryActivity.BulkEntryItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun getItems() = items

    fun onDestroy() {
        voiceInputHelper.destroy()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BulkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bulk_entry, parent, false)
        return BulkViewHolder(view)
    }

    override fun onBindViewHolder(holder: BulkViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class BulkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvCustomerName)
        private val tvBrand: TextView = itemView.findViewById(R.id.tvDefaultBrand)
        private val etQty: TextInputEditText = itemView.findViewById(R.id.etQuantity)
        private val tilQty: TextInputLayout = itemView.findViewById(R.id.tilQuantity)

        fun bind(item: HistoricalEntryActivity.BulkEntryItem) {
            tvName.text = item.customerName
            tvBrand.text = item.brandName
            etQty.setText(item.quantity.toString())
            
            voiceInputHelper.attachTo(tilQty)

            etQty.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    item.quantity = s.toString().toDoubleOrNull() ?: 0.0
                }
            })
        }
    }
}
