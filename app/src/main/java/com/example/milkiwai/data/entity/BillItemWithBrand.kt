package com.example.milkiwai.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class BillItemWithBrand(
    @Embedded
    val billItem: BillItem,

    @Relation(
        parentColumn = "brandId",
        entityColumn = "id"
    )
    val brand: MilkBrand?
)
