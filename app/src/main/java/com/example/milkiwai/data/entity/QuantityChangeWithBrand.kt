package com.example.milkiwai.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class QuantityChangeWithBrand(
    @Embedded
    val quantityChange: QuantityChange,

    @Relation(
        parentColumn = "brandId",
        entityColumn = "id"
    )
    val brand: MilkBrand?
)
