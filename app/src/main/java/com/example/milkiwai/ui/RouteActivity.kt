package com.example.milkiwai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.data.entity.Customer
import com.example.milkiwai.viewmodel.CustomerViewModel
import com.google.android.material.appbar.MaterialToolbar

class RouteActivity : AppCompatActivity() {

    private val viewModel: CustomerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val rvRoute = findViewById<RecyclerView>(R.id.rvRoute)
        val adapter = RouteAdapter()
        rvRoute.adapter = adapter

        viewModel.allCustomers.observe(this) { customers ->
            // Simple Optimization: Sort by Area then Building Name
            val optimizedRoute = customers.sortedWith(compareBy({ it.area }, { it.buildingName }))
            adapter.setCustomers(optimizedRoute)
        }
    }

    class RouteAdapter : RecyclerView.Adapter<RouteAdapter.RouteViewHolder>() {
        private var customers = listOf<Customer>()

        fun setCustomers(newCustomers: List<Customer>) {
            customers = newCustomers
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_route, parent, false)
            return RouteViewHolder(view)
        }

        override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
            holder.bind(customers[position], position + 1)
        }

        override fun getItemCount(): Int = customers.size

        class RouteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val tvStopNumber: TextView = view.findViewById(R.id.tvStopNumber)
            private val tvLocationName: TextView = view.findViewById(R.id.tvLocationName)
            private val tvLandmark: TextView = view.findViewById(R.id.tvLandmark)

            fun bind(customer: Customer, stop: Int) {
                tvStopNumber.text = stop.toString()
                tvLocationName.text = "${customer.area} - ${customer.buildingName}"
                tvLandmark.text = customer.landmark
            }
        }
    }
}
