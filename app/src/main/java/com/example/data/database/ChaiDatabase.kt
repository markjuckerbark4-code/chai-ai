package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ChaiDao
import com.example.data.model.BotEntity
import com.example.data.model.MessageEntity
import com.example.data.model.PersonaEntity

@Database(
    entities = [BotEntity::class, MessageEntity::class, PersonaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ChaiDatabase : RoomDatabase() {
    abstract fun chaiDao(): ChaiDao

    companion object {
        @Volatile
        private var INSTANCE: ChaiDatabase? = null

        fun getDatabase(context: Context): ChaiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChaiDatabase::class.java,
                    "chai_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
