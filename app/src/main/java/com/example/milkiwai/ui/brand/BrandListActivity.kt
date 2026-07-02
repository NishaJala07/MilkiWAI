package com.example.milkiwai.ui.brand

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.viewmodel.MilkBrandViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BrandListActivity : AppCompatActivity() {

    private val viewModel: MilkBrandViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brand_list)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val rvBrands = findViewById<RecyclerView>(R.id.rvBrands)
        val fabAddBrand = findViewById<FloatingActionButton>(R.id.fabAddBrand)
        val emptyState = findViewById<LinearLayout>(R.id.emptyState)

        val adapter = BrandAdapter(
            onEdit = { brand ->
                val intent = Intent(this, AddBrandActivity::class.java).apply {
                    putExtra("BRAND_ID", brand.id)
                    putExtra("BRAND_NAME", brand.brandName)
                    putExtra("BRAND_PRICE", brand.pricePerLitre)
                }
                startActivity(intent)
            },
            onDelete = { brand ->
                AlertDialog.Builder(this)
                    .setTitle("Delete ${brand.brandName}?")
                    .setMessage("Are you sure you want to delete this brand?")
                    .setPositiveButton("YES") { _, _ ->
                        viewModel.delete(brand)
                        Toast.makeText(this, "Brand deleted", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("NO", null)
                    .show()
            }
        )

        rvBrands.adapter = adapter
        rvBrands.layoutManager = LinearLayoutManager(this)

        viewModel.allBrands.observe(this) { brands ->
            if (brands.isEmpty()) {
                emptyState.visibility = View.VISIBLE
                rvBrands.visibility = View.GONE
            } else {
                emptyState.visibility = View.GONE
                rvBrands.visibility = View.VISIBLE
                adapter.submitList(brands)
            }
        }

        fabAddBrand.setOnClickListener {
            startActivity(Intent(this, AddBrandActivity::class.java))
        }
    }
}
