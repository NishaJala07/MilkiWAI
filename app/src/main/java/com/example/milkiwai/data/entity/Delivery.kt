package com.example.milkiwai.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deliveries")
data class Delivery(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val customerId: Int,
    val brandId: Int,
    val quantity: Double,
    val deliveryDate: String, // Format: "dd MMM yyyy"
    val status: String, // DELIVERED, MISSED, CANCELLED
    val remarks: String = ""
)
