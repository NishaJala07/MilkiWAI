package com.example.milkiwai.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.net.URLEncoder

class WhatsAppHelper(private val context: Context) {

    fun sendBillPdf(mobile: String, pdfPath: String, message: String) {
        val file = File(pdfPath)
        if (!file.exists()) {
            Toast.makeText(context, "PDF file not found", Toast.LENGTH_SHORT).show()
            return
        }

        val pdfUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            putExtra(Intent.EXTRA_TEXT, message)
            setPackage("com.whatsapp")
            // Try to pre-select the customer mobile number if valid
            if (mobile.length >= 10) {
                val cleanMobile = mobile.replace("+", "").replace(" ", "")
                val target = if (cleanMobile.length == 10) "91$cleanMobile" else cleanMobile
                putExtra("jid", "$target@s.whatsapp.net")
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // If WhatsApp is not installed, fallback to generic share
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, pdfUri)
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(fallbackIntent, "Share Bill via"))
        }
    }
}
