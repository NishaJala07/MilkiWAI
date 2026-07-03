package com.example.milkiwai.ui.billing

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.milkiwai.R
import com.example.milkiwai.data.entity.MonthlyBill
import com.example.milkiwai.viewmodel.BillViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.example.milkiwai.utils.WhatsAppHelper
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.text.DateFormatSymbols
import java.util.*

class BillDetailsActivity : AppCompatActivity() {

    private val viewModel: BillViewModel by viewModels()
    private lateinit var whatsAppHelper: WhatsAppHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_details)

        whatsAppHelper = WhatsAppHelper(this)

        val billId = intent.getIntExtra("BILL_ID", -1)
        val customerName = intent.getStringExtra("CUSTOMER_NAME") ?: "Valued Customer"
        val customerMobile = intent.getStringExtra("CUSTOMER_MOBILE") ?: ""
        val month = intent.getIntExtra("MONTH", 0)
        val year = intent.getIntExtra("YEAR", 0)
        val total = intent.getDoubleExtra("TOTAL", 0.0)
        val status = intent.getStringExtra("STATUS") ?: "PENDING"
        val pdfPath = intent.getStringExtra("PDF_PATH") ?: ""

        if (billId == -1) {
            finish()
            return
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        findViewById<TextView>(R.id.tvCustomerName).text = customerName
        findViewById<TextView>(R.id.tvCustomerDetails).text = "Mobile: +91 $customerMobile"
        
        val monthName = if (month > 0) DateFormatSymbols().months[month - 1] else "--"
        findViewById<TextView>(R.id.tvBillPeriod).text = "$monthName $year"
        findViewById<TextView>(R.id.tvTotalAmount).text = "₹${total.toInt()}"

        val tvStatusBadge = findViewById<TextView>(R.id.tvStatusBadge)
        val btnMarkPaid = findViewById<Button>(R.id.btnMarkPaid)
        
        if (status == "PAID") {
            tvStatusBadge.text = "PAID"
            tvStatusBadge.setTextColor(Color.parseColor("#43A047"))
            tvStatusBadge.setBackgroundColor(Color.parseColor("#E8F5E9"))
            btnMarkPaid.visibility = View.GONE
        } else {
            tvStatusBadge.text = "UNPAID"
            tvStatusBadge.setTextColor(Color.parseColor("#E53935"))
            tvStatusBadge.setBackgroundColor(Color.parseColor("#FFEBEE"))
            btnMarkPaid.visibility = View.VISIBLE
        }

        val rvItems = findViewById<RecyclerView>(R.id.rvBillItems)
        val adapter = BillItemAdapter()
        rvItems.adapter = adapter
        rvItems.layoutManager = LinearLayoutManager(this)

        val tvConsumption = findViewById<TextView>(R.id.tvConsumptionSummary)

        viewModel.getItemsForBill(billId).observe(this) { items ->
            adapter.setItems(items)
            val totalQty = items.sumOf { it.billItem.totalQuantity }
            tvConsumption.text = "Total Milk Consumption: $totalQty Litres"
        }

        findViewById<Button>(R.id.btnMarkPaid).setOnClickListener {
            viewModel.markAsPaid(billId)
            tvStatusBadge.text = "PAID"
            tvStatusBadge.setTextColor(Color.parseColor("#43A047"))
            tvStatusBadge.setBackgroundColor(Color.parseColor("#E8F5E9"))
            btnMarkPaid.visibility = View.GONE
            Toast.makeText(this, "Bill marked as PAID", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnViewPdf).setOnClickListener {
            if (pdfPath.isNotEmpty()) openPdf(pdfPath) else Toast.makeText(this, "PDF not found", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnShare).setOnClickListener {
            if (pdfPath.isNotEmpty()) {
                val message = "Hello $customerName,\n\nYour $monthName Milk Bill is Ready.\nAmount: ₹$total\n\nPlease find attached bill.\n\nThank You."
                whatsAppHelper.sendBillPdf(customerMobile, pdfPath, message)
            } else {
                Toast.makeText(this, "PDF not found", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnEmail).setOnClickListener {
            if (pdfPath.isNotEmpty()) shareViaEmail(pdfPath, customerName, monthName, year, total) else Toast.makeText(this, "PDF not found", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnQrPay).setOnClickListener {
            showQrDialog(customerName, total)
        }
    }

    private fun showQrDialog(name: String, amount: Double) {
        val upiId = "dairy@upi" // In a real app, this would be from settings
        val upiUri = "upi://pay?pa=$upiId&pn=MilkiWAI&am=$amount&cu=INR&tn=Milk Bill for $name"
        
        val bitmap = generateQrCode(upiUri)
        if (bitmap != null) {
            val dialogView = layoutInflater.inflate(R.layout.dialog_qr_payment, null)
            val ivQr = dialogView.findViewById<ImageView>(R.id.ivQrCode)
            val tvAmount = dialogView.findViewById<TextView>(R.id.tvQrAmount)
            ivQr.setImageBitmap(bitmap)
            tvAmount.text = "Amount: ₹$amount"
            
            AlertDialog.Builder(this)
                .setTitle("Scan to Pay")
                .setView(dialogView)
                .setPositiveButton("Done", null)
                .show()
        }
    }

    private fun generateQrCode(text: String): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    private fun shareViaEmail(path: String, name: String, month: String, year: Int, amount: Double) {
        val file = File(path)
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
            val message = "Hello $name,\n\nYour Milk Bill for $month $year is attached.\nTotal Amount: ₹$amount\n\nThank You."
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, message)
                putExtra(Intent.EXTRA_SUBJECT, "Milk Bill - $month $year")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Send Email..."))
        }
    }

    private fun openPdf(path: String) {
        val file = File(path)
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(intent)
        }
    }
}
