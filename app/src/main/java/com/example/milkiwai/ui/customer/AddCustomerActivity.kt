package com.example.milkiwai.ui.customer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.milkiwai.R
import com.example.milkiwai.data.entity.Customer
import com.example.milkiwai.data.entity.CustomerSubscription
import com.example.milkiwai.data.entity.MilkBrand
import com.example.milkiwai.viewmodel.CustomerViewModel
import com.example.milkiwai.viewmodel.MilkBrandViewModel
import com.example.milkiwai.viewmodel.SubscriptionViewModel
import com.example.milkiwai.utils.VoiceInputHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.textfield.TextInputLayout
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.google.android.gms.location.LocationServices
import com.google.android.material.appbar.MaterialToolbar

class AddCustomerActivity : AppCompatActivity() {

    private lateinit var viewModel: CustomerViewModel
    private lateinit var brandViewModel: MilkBrandViewModel
    private lateinit var subViewModel: SubscriptionViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var voiceInputHelper: VoiceInputHelper
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var customerId: Int = 0
    private var isEdit: Boolean = false
    private var allBrands: List<MilkBrand> = emptyList()
    private var selectedBrandId: Int = -1
    private var existingSubscriptionId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_customer)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        isEdit = intent.getBooleanExtra("IS_EDIT", false)
        customerId = intent.getIntExtra("CUSTOMER_ID", 0)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
        if (isEdit) {
            toolbar.title = "Edit Customer"
        }

        viewModel = ViewModelProvider(this).get(CustomerViewModel::class.java)
        brandViewModel = ViewModelProvider(this).get(MilkBrandViewModel::class.java)
        subViewModel = ViewModelProvider(this).get(SubscriptionViewModel::class.java)
        voiceInputHelper = VoiceInputHelper(this)

        val etName = findViewById<EditText>(R.id.etName)
        val etBuilding = findViewById<EditText>(R.id.etBuilding)
        val etRoomNo = findViewById<EditText>(R.id.etRoomNo)
        val etMobile = findViewById<EditText>(R.id.etMobile)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etArea = findViewById<EditText>(R.id.etArea)
        val etLandmark = findViewById<EditText>(R.id.etLandmark)
        val etQuantity = findViewById<EditText>(R.id.etQuantity)
        val spinnerBrands = findViewById<AutoCompleteTextView>(R.id.spinnerBrands)
        val tvLocationStatus = findViewById<TextView>(R.id.tvLocationStatus)
        val btnGetLocation = findViewById<Button>(R.id.btnGetLocation)
        val btnSave = findViewById<Button>(R.id.btnSave)
        
        // Attach Voice Input
        voiceInputHelper.attachTo(findViewById(R.id.tilName))
        voiceInputHelper.attachTo(findViewById(R.id.tilMobile))
        voiceInputHelper.attachTo(findViewById(R.id.tilEmail))
        voiceInputHelper.attachTo(findViewById(R.id.tilQuantity))
        voiceInputHelper.attachTo(findViewById(R.id.tilBuilding))
        voiceInputHelper.attachTo(findViewById(R.id.tilRoomNo))
        voiceInputHelper.attachTo(findViewById(R.id.tilArea))
        voiceInputHelper.attachTo(findViewById(R.id.tilLandmark))

        // Populate Brands
        brandViewModel.allBrands.observe(this) { brands ->
            allBrands = brands
            val brandNames = brands.map { it.brandName }
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, brandNames)
            spinnerBrands.setAdapter(adapter)
            
            // If in edit mode, ensure the selected brand is shown correctly
            if (isEdit && selectedBrandId != -1) {
                val brand = brands.find { it.id == selectedBrandId }
                brand?.let { spinnerBrands.setText(it.brandName, false) }
            }
        }

        spinnerBrands.setOnItemClickListener { _, _, position, _ ->
            selectedBrandId = allBrands[position].id
        }

        if (isEdit) {
            viewModel.allCustomers.observe(this) { customers ->
                customers.find { it.id == customerId }?.let { customer ->
                    etName.setText(customer.name)
                    etBuilding.setText(customer.buildingName)
                    etRoomNo.setText(customer.roomNo)
                    etMobile.setText(customer.mobile)
                    etEmail.setText(customer.email)
                    etArea.setText(customer.area)
                    etLandmark.setText(customer.landmark)
                    latitude = customer.latitude
                    longitude = customer.longitude
                    if (latitude != 0.0) {
                        tvLocationStatus.text = "Location Captured: ${String.format("%.4f, %.4f", latitude, longitude)}"
                    }
                    btnSave.text = "Update Customer"
                }
            }
            
            subViewModel.getSubscriptions(customerId).observe(this) { subs ->
                if (subs.isNotEmpty()) {
                    val primarySub = subs[0]
                    existingSubscriptionId = primarySub.subscription.id
                    selectedBrandId = primarySub.brand?.id ?: -1
                    spinnerBrands.setText(primarySub.brand?.brandName ?: "", false)
                    etQuantity.setText(primarySub.subscription.quantity.toString())
                }
            }
        }

        intent.getStringExtra("VOICE_NAME")?.let { etName.setText(it) }
        intent.getStringExtra("VOICE_BUILDING")?.let { etBuilding.setText(it) }
        intent.getStringExtra("VOICE_ROOM")?.let { etRoomNo.setText(it) }
        intent.getStringExtra("VOICE_MOBILE")?.let { etMobile.setText(it) }

        btnGetLocation.setOnClickListener {
            getLocation(tvLocationStatus)
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val building = etBuilding.text.toString().trim()
            val roomNo = etRoomNo.text.toString().trim()
            val mobile = etMobile.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val area = etArea.text.toString().trim()
            val landmark = etLandmark.text.toString().trim()
            val quantityStr = etQuantity.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Name cannot be empty"
                return@setOnClickListener
            }

            if (mobile.length != 10 || !TextUtils.isDigitsOnly(mobile)) {
                etMobile.error = "Enter valid 10-digit mobile number"
                return@setOnClickListener
            }

            if (selectedBrandId == -1) {
                Toast.makeText(this, "Please select a Milk Brand", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val quantity = quantityStr.toDoubleOrNull() ?: 0.0
            if (quantity <= 0) {
                etQuantity.error = "Enter valid quantity"
                return@setOnClickListener
            }

            val customer = Customer(
                id = if (isEdit) customerId else 0,
                name = name,
                buildingName = building,
                roomNo = roomNo,
                mobile = mobile,
                email = email,
                area = area,
                landmark = landmark,
                latitude = latitude,
                longitude = longitude
            )

            lifecycleScope.launch {
                if (isEdit) {
                    viewModel.update(customer)
                    
                    val subscription = CustomerSubscription(
                        id = existingSubscriptionId,
                        customerId = customerId,
                        brandId = selectedBrandId,
                        quantity = quantity,
                        isActive = true
                    )
                    subViewModel.update(subscription)
                    
                    Toast.makeText(this@AddCustomerActivity, "Customer Updated", Toast.LENGTH_SHORT).show()
                } else {
                    val newCustomerId = viewModel.insertCustomer(customer).toInt()
                    
                    val subscription = CustomerSubscription(
                        customerId = newCustomerId,
                        brandId = selectedBrandId,
                        quantity = quantity,
                        isActive = true
                    )
                    subViewModel.insert(subscription)
                    
                    Toast.makeText(this@AddCustomerActivity, "Customer Saved", Toast.LENGTH_SHORT).show()
                }
                finish()
            }
        }
    }

    private fun getLocation(tvLocationStatus: TextView) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                tvLocationStatus.text = "Location Captured: ${String.format("%.4f, %.4f", latitude, longitude)}"
            } else {
                Toast.makeText(this, "Unable to get location. Ensure GPS is on.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation(findViewById(R.id.tvLocationStatus))
        }
    }

    override fun onDestroy() {
        voiceInputHelper.destroy()
        super.onDestroy()
    }
}
