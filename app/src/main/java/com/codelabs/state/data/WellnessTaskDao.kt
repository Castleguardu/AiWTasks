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
    // 修改查询语句，只返回未完成的任务
    @Query("SELECT * FROM wellness_tasks WHERE checked = 0")
    fun getAllActiveTasks(): Flow<List<WellnessTask>>

    // 新增：统计已完成任务数量
    @Query("SELECT COUNT(*) FROM wellness_tasks WHERE checked = 1")
    fun getCompletedTasksCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: WellnessTask)

    @Update
    suspend fun update(task: WellnessTask)

    @Delete
    suspend fun delete(task: WellnessTask)
}
