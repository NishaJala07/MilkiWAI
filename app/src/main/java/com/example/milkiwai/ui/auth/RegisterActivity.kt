package com.example.milkiwai.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.milkiwai.R
import com.example.milkiwai.utils.VoiceInputHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var voiceInputHelper: VoiceInputHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<EditText>(R.id.etRegName)
        val etEmail = findViewById<EditText>(R.id.etRegEmail)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val btnRegister = findViewById<Button>(R.id.btnDoRegister)
        val btnBackToLogin = findViewById<Button>(R.id.btnBackToLogin)
        val progressBar = findViewById<ProgressBar>(R.id.regProgressBar)
        
        voiceInputHelper = VoiceInputHelper(this)
        voiceInputHelper.attachTo(findViewById(R.id.tilRegName))
        voiceInputHelper.attachTo(findViewById(R.id.tilRegEmail))

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnRegister.isEnabled = false

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        
                        // Update Profile Name in Auth
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                        
                        user?.updateProfile(profileUpdates)

                        // Initialize User Document in Firestore
                        val userData = mapOf(
                            "name" to name,
                            "email" to email,
                            "createdAt" to com.google.firebase.Timestamp.now()
                        )
                        
                        user?.let {
                            db.collection("users").document(it.uid).set(userData)
                        }

                        Toast.makeText(this, "Registration Successful! Please Login.", Toast.LENGTH_LONG).show()
                        
                        // Sign out and go to login
                        auth.signOut()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        progressBar.visibility = View.GONE
                        btnRegister.isEnabled = true
                        Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        btnBackToLogin.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        voiceInputHelper.destroy()
        super.onDestroy()
    }
}
