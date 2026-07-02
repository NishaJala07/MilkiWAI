package com.example.milkiwai.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.milkiwai.data.entity.VoiceHistory

@Dao
interface VoiceHistoryDao {
    @Insert
    suspend fun insert(history: VoiceHistory)

    @Query("SELECT * FROM voice_history ORDER BY id DESC")
    fun getAllHistory(): LiveData<List<VoiceHistory>>
}
