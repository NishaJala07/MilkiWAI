package com.example.milkiwai.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_transactions")
data class DailyTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val customerId: Int,
    val brandId: Int,
    val transactionDate: String, // Format: dd MMM yyyy
    val quantity: Double
)
