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
    @Query("SELECT * FROM wellness_tasks")
    fun getAll(): Flow<List<WellnessTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: WellnessTask)

    @Update
    suspend fun update(task: WellnessTask)

    @Delete
    suspend fun delete(task: WellnessTask)
}
