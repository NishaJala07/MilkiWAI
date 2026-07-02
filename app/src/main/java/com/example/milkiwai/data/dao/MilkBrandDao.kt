package com.example.milkiwai.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.milkiwai.data.entity.MilkBrand

@Dao
interface MilkBrandDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrand(brand: MilkBrand): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(brands: List<MilkBrand>)

    @Update
    suspend fun updateBrand(brand: MilkBrand)

    @Delete
    suspend fun deleteBrand(brand: MilkBrand)

    @Query("SELECT * FROM milk_brands")
    fun getAllBrands(): LiveData<List<MilkBrand>>

    @Query("SELECT * FROM milk_brands")
    suspend fun getAllBrandsList(): List<MilkBrand>
}
