package com.example.milkiwai.ui.billing

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.milkiwai.R
import com.example.milkiwai.data.entity.MonthlyBill
import com.example.milkiwai.viewmodel.BillViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.text.SimpleDateFormat
import java.util.*

class PaymentActivity : AppCompatActivity() {

    private val viewModel: BillViewModel by viewModels()
    private var billId: Int = -1
    private var amount: Double = 0.0
    private var customerName: String = ""
    private var monthYear: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        billId = intent.getIntExtra("BILL_ID", -1)
        amount = intent.getDoubleExtra("AMOUNT", 0.0)
        customerName = intent.getStringExtra("CUSTOMER_NAME") ?: "Customer"
        monthYear = intent.getStringExtra("MONTH_YEAR") ?: ""

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        findViewById<TextView>(R.id.tvCustomerName).text = "Customer: $customerName"
        findViewById<TextView>(R.id.tvBillDetails).text = "Bill: $monthYear"
        findViewById<TextView>(R.id.tvAmount).text = "₹$amount"

        val ivQrCode = findViewById<ImageView>(R.id.ivQrCode)
        val btnMarkPaid = findViewById<Button>(R.id.btnMarkPaid)

        val prefs = getSharedPreferences("milkiwai_prefs", MODE_PRIVATE)
        val upiId = prefs.getString("upi_id", "") ?: ""

        if (upiId.isNotEmpty()) {
            val upiUri = "upi://pay?pa=$upiId&pn=MilkWAI&am=$amount&cu=INR"
            val qrBitmap = generateQrCode(upiUri)
            ivQrCode.setImageBitmap(qrBitmap)
        } else {
            Toast.makeText(this, "Please set UPI ID in Settings first", Toast.LENGTH_LONG).show()
        }

        btnMarkPaid.setOnClickListener {
            markAsPaid()
        }
    }

    private fun generateQrCode(text: String): Bitmap? {
        return try {
            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 512, 512)
            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.createBitmap(bitMatrix)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun markAsPaid() {
        val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val transactionId = "UPI_${System.currentTimeMillis()}"
        
        // Use a one-time observation or better yet, get from intent if we pass the whole bill
        // For simplicity, let's just use the current observation but make it safer
        viewModel.allBills.observe(this) { bills ->
            val bill = bills.find { it.bill.id == billId }?.bill
            if (bill != null && bill.paymentStatus != "PAID") {
                val updatedBill = bill.copy(
                    paymentStatus = "PAID",
                    paymentMode = "UPI",
                    transactionId = transactionId,
                    paymentDate = today
                )
                viewModel.updateBill(updatedBill)
                Toast.makeText(this, "Payment Recorded Successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
