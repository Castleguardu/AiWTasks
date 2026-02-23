package com.codelabs.state.data.repository

import android.util.Log
import com.codelabs.state.data.Milestone
import com.codelabs.state.data.MilestoneDao
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
    // Task
    fun getAllActiveTasks(): Flow<List<WellnessTask>>
    fun getAllCompletedTasks(): Flow<List<WellnessTask>>
    suspend fun addTask(title: String, startTimeMillis: Long, endTimeMillis: Long, rrule: String?)
    suspend fun deleteTask(task: WellnessTask)
    suspend fun updateTask(task: WellnessTask)
    suspend fun completeTaskAndSync(task: WellnessTask)
    fun getCompletedTasksCount(): Flow<Int>

    // UserStats
    fun getUserStats(): Flow<UserStats> 
    suspend fun updateUserName(newName: String) // 新增

    // Shop
    fun getAllRewards(): Flow<List<RewardItem>>
    suspend fun addReward(item: RewardItem)
    suspend fun deleteReward(item: RewardItem)
    suspend fun updatePlayerGold(newGold: Int) 
    suspend fun ensureDefaultRewards() 

    // Milestones (新增)
    fun getAllMilestones(): Flow<List<Milestone>>
    suspend fun addMilestone(title: String, max: Int)
    suspend fun updateMilestoneProgress(milestone: Milestone)
}

class DefaultTaskRepository(
    private val taskDao: WellnessTaskDao,
    private val userStatsDao: UserStatsDao,
    private val rewardItemDao: RewardItemDao,
    private val milestoneDao: MilestoneDao, // 新增注入
    private val calendarDataSource: CalendarDataSource,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val reminderManager: ReminderManager
) : TaskRepository {

    // ... (Tasks methods remain unchanged)
    override fun getAllActiveTasks(): Flow<List<WellnessTask>> = taskDao.getAllActiveTasks()
    override fun getAllCompletedTasks(): Flow<List<WellnessTask>> = taskDao.getAllCompletedTasks()
    override fun getCompletedTasksCount(): Flow<Int> = taskDao.getCompletedTasksCount()

    override suspend fun addTask(title: String, startTimeMillis: Long, endTimeMillis: Long, rrule: String?) {
        var calendarEventId: Long? = null
        try {
            calendarEventId = calendarDataSource.addEvent(title, startTimeMillis, endTimeMillis, rrule)
        } catch (e: Exception) { Log.e("Repo", "Cal err", e) }
        val newTask = WellnessTask(label = title, timeInMillis = startTimeMillis, rrule = rrule, checked = false, calendarEventId = calendarEventId)
        val rowId = taskDao.insert(newTask)
        reminderManager.scheduleReminder(newTask.copy(id = rowId.toInt()))
    }

    override suspend fun deleteTask(task: WellnessTask) {
        task.calendarEventId?.let { try { calendarDataSource.deleteEvent(it) } catch (e: Exception){} }
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
            task.calendarEventId?.let { 
                try { if (!task.label.startsWith("✅")) calendarDataSource.updateEventTitle(it, "✅ ${task.label}") } catch (e: Exception){} 
            }
        } catch (e: Exception) { throw e }
    }

    // UserStats
    override fun getUserStats(): Flow<UserStats> = userStatsDao.getUserStatsFlow().map { it ?: UserStats() }
    
    override suspend fun updateUserName(newName: String) {
        val currentStats = userStatsDao.getUserStats() ?: UserStats()
        userStatsDao.insertOrUpdate(currentStats.copy(userName = newName))
    }

    // Shop
    override fun getAllRewards() = rewardItemDao.getAllRewards()
    override suspend fun addReward(item: RewardItem) = rewardItemDao.insert(item)
    override suspend fun deleteReward(item: RewardItem) = rewardItemDao.delete(item)
    override suspend fun updatePlayerGold(newGold: Int) {
        val stats = userStatsDao.getUserStats() ?: UserStats()
        userStatsDao.insertOrUpdate(stats.copy(gold = newGold))
    }
    override suspend fun ensureDefaultRewards() {
        if (rewardItemDao.count() == 0) {
            listOf(
                RewardItem(title = "给自己放假半天", cost = 500),
                RewardItem(title = "吃顿好的", cost = 200),
                RewardItem(title = "买喜欢的游戏", cost = 1000),
                RewardItem(title = "喝一杯奶茶", cost = 50)
            ).forEach { rewardItemDao.insert(it) }
        }
    }

    // Milestones
    override fun getAllMilestones(): Flow<List<Milestone>> = milestoneDao.getAllMilestones()
    
    override suspend fun addMilestone(title: String, max: Int) {
        milestoneDao.insert(Milestone(title = title, maxProgress = max))
    }
    
    override suspend fun updateMilestoneProgress(milestone: Milestone) {
        milestoneDao.update(milestone)
    }
}
