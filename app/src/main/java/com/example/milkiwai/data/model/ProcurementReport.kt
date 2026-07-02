package com.example.milkiwai.data.model

data class ProcurementReport(
    val brandId: Int,
    val brandName: String,
    val requiredQuantity: Double,
    val suggestedQuantity: Double
)
