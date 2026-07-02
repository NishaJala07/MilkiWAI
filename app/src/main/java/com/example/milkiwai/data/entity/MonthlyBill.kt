package com.example.milkiwai.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_bills")
data class MonthlyBill(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val customerId: Int,
    val month: Int,
    val year: Int,
    val totalAmount: Double,
    val billDate: String,
    val paymentStatus: String = "PENDING",
    val pdfPath: String = "",
    val paymentMode: String = "",
    val transactionId: String = "",
    val paymentDate: String = ""
)
