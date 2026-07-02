package com.example.milkiwai.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "customer_subscriptions",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MilkBrand::class,
            parentColumns = ["id"],
            childColumns = ["brandId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CustomerSubscription(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val customerId: Int,
    val brandId: Int,
    val quantity: Double,
    val isActive: Boolean = true
)
