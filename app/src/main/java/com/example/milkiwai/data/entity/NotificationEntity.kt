package com.example.milkiwai.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val customerId: Int,
    val title: String,
    val message: String,
    val type: String, // PAYMENT_REMINDER, BILL_GENERATED, DELIVERY_ALERT, LOW_STOCK, SYSTEM
    val createdDate: String,
    val status: String // SENT, READ, PENDING
)
