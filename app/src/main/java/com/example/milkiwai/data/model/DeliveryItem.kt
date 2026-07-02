package com.example.milkiwai.data.model

import com.example.milkiwai.data.entity.Delivery
import com.example.milkiwai.data.entity.Customer
import com.example.milkiwai.data.entity.MilkBrand

data class DeliveryItem(
    val customer: Customer,
    val brand: MilkBrand,
    var delivery: Delivery? = null,
    val scheduledQuantity: Double
)
