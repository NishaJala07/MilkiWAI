package com.example.milkiwai.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_history")
data class VoiceHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val command: String,
    val date: String,
    val status: String
)
