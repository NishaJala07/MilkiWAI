package com.example.milkiwai.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quantity_changes")
data class QuantityChange(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val customerId: Int,
    val brandId: Int,
    val oldQuantity: Double,
    val newQuantity: Double,
    val startDate: String,
    val endDate: String,
    val remarks: String = "",
    val status: String = "ACTIVE"
)
