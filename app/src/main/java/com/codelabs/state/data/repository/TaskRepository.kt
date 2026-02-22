package com.codelabs.state.data.repository

import android.util.Log
import com.codelabs.state.data.RewardItem
import com.codelabs.state.data.RewardItemDao
import com.codelabs.state.data.UserStats
import com.codelabs.state.data.UserStatsDao
import com.codelabs.state.data.WellnessTask
import com.codelabs.state.data.WellnessTaskDao
import com.codelabs.state.data.source.CalendarDataSource
import com.codelabs.state.domain.CompleteTaskUseCase
import com.codelabs.state.utils.ReminderManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface TaskRepository {
    fun getAllActiveTasks(): Flow<List<WellnessTask>> // 重命名以明确意图
    fun getAllCompletedTasks(): Flow<List<WellnessTask>> // 新增
    fun getUserStats(): Flow<UserStats> 
    fun getCompletedTasksCount(): Flow<Int>
    
    suspend fun addTask(title: String, startTimeMillis: Long, endTimeMillis: Long, rrule: String?)
    suspend fun deleteTask(task: WellnessTask)
    suspend fun updateTask(task: WellnessTask)
    suspend fun completeTaskAndSync(task: WellnessTask)

    fun getAllRewards(): Flow<List<RewardItem>>
    suspend fun addReward(item: RewardItem)
    suspend fun deleteReward(item: RewardItem)
    suspend fun updatePlayerGold(newGold: Int) 
    suspend fun ensureDefaultRewards() 
}

class DefaultTaskRepository(
    private val taskDao: WellnessTaskDao,
    private val userStatsDao: UserStatsDao,
    private val rewardItemDao: RewardItemDao,
    private val calendarDataSource: CalendarDataSource,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val reminderManager: ReminderManager
) : TaskRepository {

    override fun getAllActiveTasks(): Flow<List<WellnessTask>> = taskDao.getAllActiveTasks()
    
    override fun getAllCompletedTasks(): Flow<List<WellnessTask>> = taskDao.getAllCompletedTasks()

    override fun getUserStats(): Flow<UserStats> = userStatsDao.getUserStatsFlow()
        .map { it ?: UserStats() }

    override fun getCompletedTasksCount(): Flow<Int> = taskDao.getCompletedTasksCount()

    // ... (其他方法保持不变) ...
    override suspend fun addTask(title: String, startTimeMillis: Long, endTimeMillis: Long, rrule: String?) {
        var calendarEventId: Long? = null
        try {
            calendarEventId = calendarDataSource.addEvent(
                title = title,
                startMillis = startTimeMillis,
                endMillis = endTimeMillis,
                rrule = rrule
            )
        } catch (e: Exception) {
            Log.e("TaskRepository", "Failed to add calendar event", e)
        }
        val newTask = WellnessTask(
            label = title,
            timeInMillis = startTimeMillis,
            rrule = rrule,
            checked = false,
            calendarEventId = calendarEventId
        )
        val rowId = taskDao.insert(newTask)
        val insertedTask = newTask.copy(id = rowId.toInt())
        reminderManager.scheduleReminder(insertedTask)
    }

    override suspend fun deleteTask(task: WellnessTask) {
        task.calendarEventId?.let { eventId ->
            try {
                calendarDataSource.deleteEvent(eventId)
            } catch (e: Exception) {
                Log.e("TaskRepository", "Failed to delete calendar event: $eventId", e)
            }
        }
        reminderManager.cancelReminder(task.id)
        taskDao.delete(task)
    }

    override suspend fun updateTask(task: WellnessTask) {
        taskDao.update(task)
        reminderManager.scheduleReminder(task)
    }

    override suspend fun completeTaskAndSync(task: WellnessTask) {
        try {
            val currentStats = userStatsDao.getUserStats() ?: UserStats()
            val newStats = completeTaskUseCase(currentStats, task.expReward, task.goldReward)
            userStatsDao.insertOrUpdate(newStats)
            taskDao.update(task.copy(checked = true))
            
            reminderManager.cancelReminder(task.id)
            
            task.calendarEventId?.let { eventId ->
                try {
                    if (!task.label.startsWith("✅")) {
                        calendarDataSource.updateEventTitle(eventId, "✅ ${task.label}")
                    }
                } catch (e: Exception) {
                    Log.e("TaskRepository", "Failed to sync calendar completion for event: $eventId", e)
                }
            }
        } catch (e: Exception) {
             Log.e("TaskRepository", "Critical error completing task ${task.id}", e)
             throw e
        }
    }

    override fun getAllRewards(): Flow<List<RewardItem>> = rewardItemDao.getAllRewards()
    override suspend fun addReward(item: RewardItem) = rewardItemDao.insert(item)
    override suspend fun deleteReward(item: RewardItem) = rewardItemDao.delete(item)
    override suspend fun updatePlayerGold(newGold: Int) {
        val currentStats = userStatsDao.getUserStats() ?: UserStats()
        userStatsDao.insertOrUpdate(currentStats.copy(gold = newGold))
    }
    override suspend fun ensureDefaultRewards() {
        if (rewardItemDao.count() == 0) {
            val defaults = listOf(
                RewardItem(title = "给自己放假半天", cost = 500),
                RewardItem(title = "吃顿好的", cost = 200),
                RewardItem(title = "买喜欢的游戏", cost = 1000),
                RewardItem(title = "喝一杯奶茶", cost = 50)
            )
            defaults.forEach { rewardItemDao.insert(it) }
        }
    }
}
