package com.example.milkiwai.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class SubscriptionWithBrand(
    @Embedded
    val subscription: CustomerSubscription,

    @Relation(
        parentColumn = "brandId",
        entityColumn = "id"
    )
    val brand: MilkBrand?,

    @Relation(
        parentColumn = "customerId",
        entityColumn = "id"
    )
    val customer: Customer?
)
