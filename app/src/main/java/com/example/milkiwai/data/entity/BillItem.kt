package com.example.milkiwai.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "bill_items",
    foreignKeys = [
        ForeignKey(
            entity = MonthlyBill::class,
            parentColumns = ["id"],
            childColumns = ["billId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BillItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val billId: Int,
    val brandId: Int,
    val totalQuantity: Double,
    val ratePerLitre: Double,
    val amount: Double
)
