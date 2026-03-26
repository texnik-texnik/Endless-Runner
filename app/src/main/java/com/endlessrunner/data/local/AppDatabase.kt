package com.endlessrunner.data.local

import android.content.Context
import androidx.room.AutoMigration
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
 *
 * Версия: 1
 * Entities: PlayerProgressEntity, GameSaveEntity, AchievementEntity, LeaderboardEntryEntity
 *
 * @property playerProgressDao DAO для прогресса игрока
 * @property gameSaveDao DAO для сохранений игр
 * @property achievementDao DAO для достижений
 * @property leaderboardDao DAO для таблицы лидеров
 */
@Database(
    entities = [
        PlayerProgressEntity::class,
        GameSaveEntity::class,
        AchievementEntity::class,
        LeaderboardEntryEntity::class
    ],
    version = 1,
    exportSchema = true,
    autoMigrations = [
        // AutoMigration для будущих миграций
        // Пример: AutoMigration(from = 1, to = 2)
    ]
)
@TypeConverters(TypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * DAO для работы с прогрессом игрока.
     */
    abstract fun playerProgressDao(): PlayerProgressDao

    /**
     * DAO для работы с сохранениями игр.
     */
    abstract fun gameSaveDao(): GameSaveDao

    /**
     * DAO для работы с достижениями.
     */
    abstract fun achievementDao(): AchievementDao

    /**
     * DAO для работы с таблицей лидеров.
     */
    abstract fun leaderboardDao(): LeaderboardDao

    companion object {
        private const val DATABASE_NAME = "endless_runner_db"

        @Volatile
        private var instance: AppDatabase? = null

        /**
         * Получение экземпляра базы данных (Singleton).
         * Использует double-checked locking для потокобезопасности.
         *
         * @param context Context приложения
         * @return Экземпляр AppDatabase
         */
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        /**
         * Построение базы данных.
         */
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                // Включаем запросы в логах для отладки
                .setJournalMode(JournalMode.TRUNCATE)
                // Разрешаем запросы в главном потоке (только для отладки!)
                .allowMainThreadQueries()
                // Callback для создания БД
                .addCallback(object : Callback() {
                    override fun onCreate(db: android.database.sqlite.SQLiteDatabase) {
                        super.onCreate(db)
                        // Инициализация данных при создании БД
                        // Например, создание достижений по умолчанию
                    }

                    override fun onOpen(db: android.database.sqlite.SQLiteDatabase) {
                        super.onOpen(db)
                        // Включение FOREIGN KEY
                        db.execSQL("PRAGMA foreign_keys = ON")
                    }
                })
                .build()
        }

        /**
         * Закрытие базы данных.
         * Вызывать только при уничтожении приложения.
         */
        fun closeDatabase() {
            instance?.close()
            instance = null
        }
    }
}
