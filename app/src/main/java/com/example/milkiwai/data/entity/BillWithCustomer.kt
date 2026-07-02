package com.example.milkiwai.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class BillWithCustomer(
    @Embedded
    val bill: MonthlyBill,

    @Relation(
        parentColumn = "customerId",
        entityColumn = "id"
    )
    val customer: Customer?
)
