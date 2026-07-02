package com.example.milkiwai.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.milkiwai.data.entity.Stock

@Dao
interface StockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stock: Stock)

    @Update
    suspend fun update(stock: Stock)

    @Query("SELECT * FROM stock WHERE brandId = :brandId")
    suspend fun getStockByBrand(brandId: Int): Stock?

    @Query("SELECT * FROM stock")
    fun getAllStock(): LiveData<List<Stock>>

    @Query("SELECT SUM(availableQuantity) FROM stock")
    suspend fun getTotalStock(): Double?
}
