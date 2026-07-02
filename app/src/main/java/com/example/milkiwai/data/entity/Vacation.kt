package com.example.milkiwai.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "vacations",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Vacation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val customerId: Int,
    val startDate: String,
    val endDate: String,
    val remarks: String = ""
)
