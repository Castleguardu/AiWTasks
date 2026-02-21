package com.codelabs.state.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WellnessTask::class, UserStats::class, RewardItem::class], version = 4, exportSchema = false)
abstract class WellnessDatabase : RoomDatabase() {
    abstract fun wellnessTaskDao(): WellnessTaskDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun rewardItemDao(): RewardItemDao // 新增

    companion object {
        @Volatile
        private var Instance: WellnessDatabase? = null

        fun getDatabase(context: Context): WellnessDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, WellnessDatabase::class.java, "wellness_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
