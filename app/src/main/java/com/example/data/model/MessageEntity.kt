package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val botId: String,
    val sender: String, // "user" or "bot"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
