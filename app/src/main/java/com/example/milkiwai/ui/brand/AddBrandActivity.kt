package com.example.milkiwai.ui.brand

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.milkiwai.R
import com.example.milkiwai.data.entity.MilkBrand
import com.example.milkiwai.viewmodel.MilkBrandViewModel
import com.example.milkiwai.utils.VoiceInputHelper
import com.google.android.material.appbar.MaterialToolbar

class AddBrandActivity : AppCompatActivity() {

    private val viewModel: MilkBrandViewModel by viewModels()
    private lateinit var voiceInputHelper: VoiceInputHelper
    private var brandId: Int = 0
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_brand)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val etBrandName = findViewById<EditText>(R.id.etBrandName)
        val etPrice = findViewById<EditText>(R.id.etPrice)
        val btnSave = findViewById<Button>(R.id.btnSave)
        
        voiceInputHelper = VoiceInputHelper(this)
        voiceInputHelper.attachTo(findViewById(R.id.tilBrandName))
        voiceInputHelper.attachTo(findViewById(R.id.tilPrice))

        if (intent.hasExtra("BRAND_ID")) {
            isEditMode = true
            brandId = intent.getIntExtra("BRAND_ID", 0)
            etBrandName.setText(intent.getStringExtra("BRAND_NAME"))
            etPrice.setText(intent.getDoubleExtra("BRAND_PRICE", 0.0).toString())
            toolbar.title = "Edit Milk Brand"
            btnSave.text = "UPDATE"
        }

        btnSave.setOnClickListener {
            val name = etBrandName.text.toString().trim()
            val priceStr = etPrice.text.toString().trim()

            if (name.isEmpty()) {
                etBrandName.error = "Brand name required"
                return@setOnClickListener
            }

            if (priceStr.isEmpty()) {
                etPrice.error = "Price required"
                return@setOnClickListener
            }

            val price = priceStr.toDoubleOrNull()
            if (price == null || price <= 0) {
                etPrice.error = "Invalid price"
                return@setOnClickListener
            }

            if (isEditMode) {
                val updatedBrand = MilkBrand(id = brandId, brandName = name, pricePerLitre = price)
                viewModel.update(updatedBrand)
                Toast.makeText(this, "Brand updated", Toast.LENGTH_SHORT).show()
            } else {
                val newBrand = MilkBrand(brandName = name, pricePerLitre = price)
                viewModel.insert(newBrand)
                Toast.makeText(this, "Brand saved", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }

    override fun onDestroy() {
        voiceInputHelper.destroy()
        super.onDestroy()
    }
}
