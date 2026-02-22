package com.codelabs.state.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WellnessTaskDao {
    @Query("SELECT * FROM wellness_tasks WHERE checked = 0")
    fun getAllActiveTasks(): Flow<List<WellnessTask>>

    // 新增：查询已完成任务，按 ID 倒序（模拟时间倒序）
    @Query("SELECT * FROM wellness_tasks WHERE checked = 1 ORDER BY id DESC")
    fun getAllCompletedTasks(): Flow<List<WellnessTask>>

    @Query("SELECT COUNT(*) FROM wellness_tasks WHERE checked = 1")
    fun getCompletedTasksCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: WellnessTask): Long

    @Update
    suspend fun update(task: WellnessTask)

    @Delete
    suspend fun delete(task: WellnessTask)
}
