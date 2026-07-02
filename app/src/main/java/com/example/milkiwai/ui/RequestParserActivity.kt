package com.example.milkiwai.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.milkiwai.R
import com.example.milkiwai.data.entity.Customer
import com.example.milkiwai.data.entity.Vacation
import com.example.milkiwai.viewmodel.BillViewModel
import com.example.milkiwai.viewmodel.CustomerViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.SimpleDateFormat
import java.util.*

class RequestParserActivity : AppCompatActivity() {

    private val customerViewModel: CustomerViewModel by viewModels()
    private val billViewModel: BillViewModel by viewModels()

    private lateinit var ivSelected: ImageView
    private lateinit var tvRaw: TextView
    private lateinit var cardResult: MaterialCardView
    private lateinit var tvCustomer: TextView
    private lateinit var tvAction: TextView
    private lateinit var tvDates: TextView

    private var selectedCustomer: Customer? = null
    private var parsedStartDate: String = ""
    private var parsedEndDate: String = ""

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_parser)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        ivSelected = findViewById(R.id.ivSelectedImage)
        tvRaw = findViewById(R.id.tvRawText)
        cardResult = findViewById(R.id.cardResult)
        tvCustomer = findViewById(R.id.tvCustomer)
        tvAction = findViewById(R.id.tvAction)
        tvDates = findViewById(R.id.tvDates)

        findViewById<Button>(R.id.btnSelectImage).setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Screenshot"), PICK_IMAGE_REQUEST)
        }

        findViewById<Button>(R.id.btnApprove).setOnClickListener {
            approveRequest()
        }

        findViewById<Button>(R.id.btnReject).setOnClickListener {
            cardResult.visibility = View.GONE
            Toast.makeText(this, "Request Rejected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val uri = data.data!!
            ivSelected.setImageURI(uri)
            ivSelected.visibility = View.VISIBLE
            recognizeText(uri)
        }
    }

    private fun recognizeText(uri: Uri) {
        val image: InputImage
        try {
            image = InputImage.fromFilePath(this, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    tvRaw.text = visionText.text
                    parseText(visionText.text)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Text recognition failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseText(text: String) {
        val lowerText = text.lowercase(Locale.getDefault())
        
        // 1. Detect STOP/PAUSE Request
        if (lowerText.contains("stop") || lowerText.contains("pause")) {
            customerViewModel.allCustomers.observe(this) { customers ->
                val foundCustomer = customers.find { customer ->
                    lowerText.contains(customer.name.lowercase(Locale.getDefault()))
                }
                
                if (foundCustomer != null) {
                    selectedCustomer = foundCustomer
                    tvCustomer.text = "Customer: ${foundCustomer.name}"
                    tvAction.text = "Action: Pause Delivery"
                    
                    val dateRegex = Regex("(\\d{1,2})\\s+([a-zA-Z]+)")
                    val matches = dateRegex.findAll(text).toList()
                    
                    if (matches.size >= 2) {
                        parsedStartDate = matches[0].value + " " + Calendar.getInstance().get(Calendar.YEAR)
                        parsedEndDate = matches[1].value + " " + Calendar.getInstance().get(Calendar.YEAR)
                        tvDates.text = "Dates: $parsedStartDate - $parsedEndDate"
                        cardResult.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this, "Could not extract dates clearly", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } 
        // 2. Detect PAYMENT Proof (e.g., "Paid ₹500", "Transaction successful")
        else if (lowerText.contains("paid") || lowerText.contains("transaction") || lowerText.contains("success")) {
            val amountRegex = Regex("(?:₹|rs\\.?|inr)\\s*(\\d+(?:\\.\\d+)?)")
            val amountMatch = amountRegex.find(lowerText)
            
            if (amountMatch != null) {
                val amount = amountMatch.groupValues[1]
                tvCustomer.text = "Detected Payment Proof"
                tvAction.text = "Action: Record Payment"
                tvDates.text = "Amount: ₹$amount"
                cardResult.visibility = View.VISIBLE
                
                findViewById<Button>(R.id.btnApprove).text = "GO TO PAYMENTS"
                findViewById<Button>(R.id.btnApprove).setOnClickListener {
                    startActivity(Intent(this, com.example.milkiwai.ui.billing.BillListActivity::class.java))
                }
            }
        } else {
            Toast.makeText(this, "No recognized request detected in image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun approveRequest() {
        val customer = selectedCustomer ?: return
        
        val vacation = Vacation(
            customerId = customer.id,
            startDate = parsedStartDate,
            endDate = parsedEndDate,
            remarks = "Via WhatsApp OCR"
        )
        
        billViewModel.insertVacation(vacation)
        Toast.makeText(this, "Vacation Record Created for ${customer.name}", Toast.LENGTH_LONG).show()
        finish()
    }
}
