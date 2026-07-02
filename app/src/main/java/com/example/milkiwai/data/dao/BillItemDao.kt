package com.example.milkiwai.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.milkiwai.data.entity.BillItem

@Dao
interface BillItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: BillItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<BillItem>)

    @Transaction
    @Query("SELECT * FROM bill_items WHERE billId = :billId")
    fun getItemsWithBrandByBill(billId: Int): LiveData<List<com.example.milkiwai.data.entity.BillItemWithBrand>>

    @Query("SELECT brandId, SUM(totalQuantity) as totalQuantity, SUM(amount) as totalAmount FROM bill_items GROUP BY brandId")
    fun getBrandWiseSales(): LiveData<List<BrandSales>>

    @Query("SELECT * FROM bill_items")
    suspend fun getAllItemsSync(): List<BillItem>
}

data class BrandSales(
    val brandId: Int,
    val totalQuantity: Double,
    val totalAmount: Double
)
