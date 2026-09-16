package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bots")
data class BotEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val creatorName: String,
    val tagline: String,
    val description: String,
    val avatarDrawableName: String,
    val firstMessage: String,
    val personalityPrompt: String,
    val category: String,
    val likeCount: Int = 19000,
    val chatCount: Int = 35800,
    val isLiked: Boolean = false,
    val isFollowing: Boolean = false,
    val isUserCreated: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
