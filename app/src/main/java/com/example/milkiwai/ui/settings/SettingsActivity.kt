package com.example.milkiwai.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.milkiwai.R
import android.widget.EditText
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.milkiwai.ui.auth.LoginActivity
import com.example.milkiwai.ui.profile.ProfileActivity
import com.example.milkiwai.viewmodel.SettingsViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest

class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModels()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val tvSyncStatus = findViewById<TextView>(R.id.tvSyncStatus)
        val btnBackup = findViewById<Button>(R.id.btnBackup)
        val btnRestore = findViewById<Button>(R.id.btnRestore)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val etUpiId = findViewById<EditText>(R.id.etUpiId)
        val btnSaveSettings = findViewById<Button>(R.id.btnSaveSettings)
        
        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val tvUserEmail = findViewById<TextView>(R.id.tvUserEmail)
        val ivProfileImg = findViewById<ImageView>(R.id.ivProfileImg)
        val btnEditProfile = findViewById<View>(R.id.btnEditProfile)

        // Populate Profile
        val user = auth.currentUser
        if (user != null) {
            tvUserEmail.text = user.email
            tvUserName.text = user.displayName ?: user.email?.split("@")?.get(0) ?: "Vendor"
            
            if (user.photoUrl != null) {
                Glide.with(this).load(user.photoUrl).into(ivProfileImg)
            }

            // Fetch name from Firestore for accuracy
            FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("name")
                    if (!name.isNullOrEmpty()) {
                        tvUserName.text = name
                    }
                }
        }

        btnEditProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<View>(R.id.layoutBackup).setOnClickListener { btnBackup.performClick() }
        findViewById<View>(R.id.layoutRestore).setOnClickListener { btnRestore.performClick() }

        val prefs = getSharedPreferences("milkiwai_prefs", MODE_PRIVATE)
        etUpiId.setText(prefs.getString("upi_id", ""))

        btnSaveSettings.setOnClickListener {
            val upiId = etUpiId.text.toString().trim()
            prefs.edit().putString("upi_id", upiId).apply()
            Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launchWhenStarted {
            viewModel.loading.collectLatest { isLoading ->
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                btnBackup.isEnabled = !isLoading
                btnRestore.isEnabled = !isLoading
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.syncStatus.collectLatest { status ->
                tvSyncStatus.text = "Status: $status"
            }
        }

        btnBackup.setOnClickListener {
            viewModel.backupNow()
        }

        btnRestore.setOnClickListener {
            viewModel.restoreNow()
        }

        btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    auth.signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
