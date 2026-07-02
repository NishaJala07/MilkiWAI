package com.example.milkiwai.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.milkiwai.R
import com.example.milkiwai.utils.VoiceInputHelper
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private lateinit var etName: TextInputEditText
    private lateinit var etMobile: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etBusinessName: TextInputEditText
    private lateinit var ivProfileImage: ImageView
    private lateinit var progressBar: View
    private lateinit var voiceInputHelper: VoiceInputHelper

    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            ivProfileImage.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        etName = findViewById(R.id.etName)
        etMobile = findViewById(R.id.etMobile)
        etEmail = findViewById(R.id.etEmail)
        etBusinessName = findViewById(R.id.etBusinessName)
        ivProfileImage = findViewById(R.id.ivProfileImage)
        
        voiceInputHelper = VoiceInputHelper(this)
        voiceInputHelper.attachTo(findViewById(R.id.tilName))
        voiceInputHelper.attachTo(findViewById(R.id.tilMobile))
        voiceInputHelper.attachTo(findViewById(R.id.tilEmail))
        voiceInputHelper.attachTo(findViewById(R.id.tilBusinessName))

        loadProfileData()

        findViewById<View>(R.id.btnEditPhoto).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        findViewById<View>(R.id.btnUpdateProfile).setOnClickListener {
            if (selectedImageUri != null) {
                uploadImageAndSaveProfile()
            } else {
                updateProfile(null)
            }
        }
    }

    private fun loadProfileData() {
        val user = auth.currentUser
        if (user != null) {
            etName.setText(user.displayName)
            etEmail.setText(user.email)

            if (user.photoUrl != null) {
                Glide.with(this)
                    .load(user.photoUrl)
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .into(ivProfileImage)
            }

            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        etMobile.setText(document.getString("mobile"))
                        etBusinessName.setText(document.getString("businessName"))
                    }
                }
        }
    }

    private fun uploadImageAndSaveProfile() {
        val user = auth.currentUser ?: return
        val ref = storage.reference.child("profile_photos/${user.uid}.jpg")

        Toast.makeText(this, "Uploading photo...", Toast.LENGTH_SHORT).show()

        selectedImageUri?.let { uri ->
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { downloadUri ->
                        updateProfile(downloadUri)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Upload Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateProfile(photoUri: Uri?) {
        val name = etName.text.toString().trim()
        val mobile = etMobile.text.toString().trim()
        val businessName = etBusinessName.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        if (user != null) {
            val profileUpdatesBuilder = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
            
            if (photoUri != null) {
                profileUpdatesBuilder.setPhotoUri(photoUri)
            }

            user.updateProfile(profileUpdatesBuilder.build())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val data = mutableMapOf(
                            "name" to name,
                            "mobile" to mobile,
                            "businessName" to businessName
                        )
                        if (photoUri != null) {
                            data["photoUrl"] = photoUri.toString()
                        }

                        db.collection("users").document(user.uid).set(data, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener {
                                Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to update database", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onDestroy() {
        voiceInputHelper.destroy()
        super.onDestroy()
    }
}
