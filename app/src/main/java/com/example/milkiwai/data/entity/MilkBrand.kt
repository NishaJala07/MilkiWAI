package com.example.milkiwai.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "milk_brands")
data class MilkBrand(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val brandName: String,
    val pricePerLitre: Double
)
