package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BotEntity
import com.example.data.model.MessageEntity
import com.example.data.model.PersonaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChaiDao {
    // Bots
    @Query("SELECT * FROM bots ORDER BY timestamp DESC")
    fun getAllBots(): Flow<List<BotEntity>>

    @Query("SELECT * FROM bots WHERE id = :id LIMIT 1")
    suspend fun getBotById(id: String): BotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBots(bots: List<BotEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBot(bot: BotEntity)

    @Update
    suspend fun updateBot(bot: BotEntity)

    @Query("UPDATE bots SET isLiked = :isLiked, likeCount = likeCount + :delta WHERE id = :id")
    suspend fun updateLike(id: String, isLiked: Boolean, delta: Int)

    @Query("UPDATE bots SET isFollowing = :isFollowing WHERE creatorName = :creatorName")
    suspend fun updateFollowing(creatorName: String, isFollowing: Boolean)

    @Query("UPDATE bots SET chatCount = chatCount + 1 WHERE id = :id")
    suspend fun incrementChatCount(id: String)

    // Messages
    @Query("SELECT * FROM messages WHERE botId = :botId ORDER BY timestamp ASC")
    fun getMessagesForBot(botId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("DELETE FROM messages WHERE botId = :botId")
    suspend fun deleteMessagesForBot(botId: String)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: Long)

    @Query("UPDATE messages SET text = :newText WHERE id = :id")
    suspend fun updateMessageText(id: Long, newText: String)

    // Personas
    @Query("SELECT * FROM personas ORDER BY id ASC")
    fun getAllPersonas(): Flow<List<PersonaEntity>>

    @Query("SELECT * FROM personas WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedPersona(): PersonaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersona(persona: PersonaEntity): Long

    @Query("UPDATE personas SET isSelected = (id = :selectedId)")
    suspend fun setSelectedPersona(selectedId: Long)

    @Query("DELETE FROM personas WHERE id = :id")
    suspend fun deletePersona(id: Long)
}
