package com.example.milkiwai.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.milkiwai.data.entity.NotificationEntity

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    @Update
    suspend fun update(notification: NotificationEntity)

    @Delete
    suspend fun delete(notification: NotificationEntity)

    @Query("SELECT * FROM notifications ORDER BY id DESC")
    fun getAllNotifications(): LiveData<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE customerId = :customerId ORDER BY id DESC")
    fun getNotificationsByCustomer(customerId: Int): LiveData<List<NotificationEntity>>
}
