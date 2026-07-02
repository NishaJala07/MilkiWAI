package com.example.milkiwai.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.milkiwai.R
import com.example.milkiwai.data.entity.Customer
import com.example.milkiwai.viewmodel.CustomerViewModel
import com.example.milkiwai.viewmodel.DeliveryViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import java.util.*

class RouteMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val deliveryViewModel: DeliveryViewModel by viewModels()
    private var customerList = listOf<Customer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_map)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        findViewById<ExtendedFloatingActionButton>(R.id.btnStartNavigation).setOnClickListener {
            startNavigation()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        deliveryViewModel.deliveryItems.observe(this) { items ->
            mMap.clear()
            val customers = items.map { it.customer }.distinctBy { it.id }
            customerList = customers.sortedWith(compareBy({ it.area }, { it.buildingName }))

            customerList.forEachIndexed { index, customer ->
                if (customer.latitude != 0.0 && customer.longitude != 0.0) {
                    val pos = LatLng(customer.latitude, customer.longitude)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(pos)
                            .title("${index + 1}. ${customer.name}")
                            .snippet("${customer.buildingName}, ${customer.area}")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    )
                }
            }

            if (customerList.isNotEmpty()) {
                val firstCustomer = customerList.find { it.latitude != 0.0 }
                if (firstCustomer != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(firstCustomer.latitude, firstCustomer.longitude), 14f))
                }
            }
        }

        deliveryViewModel.loadDeliveries(Date())
    }

    private fun startNavigation() {
        if (customerList.isEmpty()) return

        val validCustomers = customerList.filter { it.latitude != 0.0 && it.longitude != 0.0 }
        if (validCustomers.isEmpty()) return

        val origin = "${validCustomers.first().latitude},${validCustomers.first().longitude}"
        val destination = "${validCustomers.last().latitude},${validCustomers.last().longitude}"
        
        val waypoints = if (validCustomers.size > 2) {
            "&waypoints=" + validCustomers.subList(1, validCustomers.size - 1)
                .joinToString("|") { "${it.latitude},${it.longitude}" }
        } else {
            ""
        }

        val uri = "https://www.google.com/maps/dir/?api=1&origin=$origin&destination=$destination$waypoints&travelmode=driving"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }
}
