package com.codelabs.state.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardItemDao {
    @Query("SELECT * FROM reward_items")
    fun getAllRewards(): Flow<List<RewardItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RewardItem)

    @Delete
    suspend fun delete(item: RewardItem)
    
    // 用于检查是否为空，方便预填充
    @Query("SELECT COUNT(*) FROM reward_items")
    suspend fun count(): Int
}
