package com.example.milkiwai.repository

import com.example.milkiwai.data.dao.MilkBrandDao
import com.example.milkiwai.data.entity.MilkBrand

class MilkBrandRepository(
    private val dao: MilkBrandDao
) {

    val allBrands = dao.getAllBrands()

    suspend fun insert(brand: MilkBrand): Long {
        return dao.insertBrand(brand)
    }

    suspend fun update(brand: MilkBrand) {
        dao.updateBrand(brand)
    }

    suspend fun delete(brand: MilkBrand) {
        dao.deleteBrand(brand)
    }
}
