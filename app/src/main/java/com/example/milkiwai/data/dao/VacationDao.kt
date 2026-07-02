package com.example.milkiwai.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.milkiwai.data.entity.Vacation

@Dao
interface VacationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVacation(vacation: Vacation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vacations: List<Vacation>)

    @Update
    suspend fun updateVacation(vacation: Vacation)

    @Delete
    suspend fun deleteVacation(vacation: Vacation)

    @Query("SELECT * FROM vacations WHERE customerId = :customerId")
    fun getVacationsByCustomer(customerId: Int): LiveData<List<Vacation>>

    @Query("SELECT * FROM vacations WHERE customerId = :customerId")
    suspend fun getVacationsByCustomerList(customerId: Int): List<Vacation>

    @Query("SELECT * FROM vacations")
    suspend fun getAllVacationsList(): List<Vacation>
}
