package com.endlessrunner.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.endlessrunner.data.local.converter.TypeConverters
import com.endlessrunner.data.local.dao.AchievementDao
import com.endlessrunner.data.local.dao.GameSaveDao
import com.endlessrunner.data.local.dao.LeaderboardDao
import com.endlessrunner.data.local.dao.PlayerProgressDao
import com.endlessrunner.data.local.entity.AchievementEntity
import com.endlessrunner.data.local.entity.GameSaveEntity
import com.endlessrunner.data.local.entity.LeaderboardEntryEntity
import com.endlessrunner.data.local.entity.PlayerProgressEntity

/**
 * Room Database для хранения игровых данных.
 */
@Database(
    entities = [
        PlayerProgressEntity::class,
        GameSaveEntity::class,
        AchievementEntity::class,
        LeaderboardEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(TypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerProgressDao(): PlayerProgressDao
    abstract fun gameSaveDao(): GameSaveDao
    abstract fun achievementDao(): AchievementDao
    abstract fun leaderboardDao(): LeaderboardDao

    companion object {
        private const val DATABASE_NAME = "endless_runner_db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .allowMainThreadQueries()
                .build()
        }

        fun closeDatabase() {
            instance?.close()
            instance = null
        }
    }
}
