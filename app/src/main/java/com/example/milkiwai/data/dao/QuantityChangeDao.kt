package com.example.milkiwai.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.milkiwai.data.entity.QuantityChange

@Dao
interface QuantityChangeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChange(change: QuantityChange)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(changes: List<QuantityChange>)

    @Update
    suspend fun updateChange(change: QuantityChange)

    @Delete
    suspend fun deleteChange(change: QuantityChange)

    @Transaction
    @Query("SELECT * FROM quantity_changes WHERE customerId = :customerId")
    fun getChangesByCustomer(customerId: Int): LiveData<List<com.example.milkiwai.data.entity.QuantityChangeWithBrand>>

    @Transaction
    @Query("SELECT * FROM quantity_changes WHERE customerId = :customerId")
    suspend fun getChangesByCustomerList(customerId: Int): List<com.example.milkiwai.data.entity.QuantityChangeWithBrand>

    @Query("SELECT * FROM quantity_changes")
    suspend fun getAllChangesList(): List<QuantityChange>
}
