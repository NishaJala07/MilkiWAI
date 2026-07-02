package com.example.milkiwai.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val buildingName: String,
    val roomNo: String,
    val mobile: String,
    val email: String,
    val area: String = "",
    val landmark: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
