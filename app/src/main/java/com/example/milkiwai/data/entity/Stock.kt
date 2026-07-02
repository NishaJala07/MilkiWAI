package com.example.milkiwai.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock")
data class Stock(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val brandId: Int,
    val availableQuantity: Double
)
