package com.codelabs.state.data.repository

import android.util.Log
import com.codelabs.state.data.UserStats
import com.codelabs.state.data.UserStatsDao
import com.codelabs.state.data.WellnessTask
import com.codelabs.state.data.WellnessTaskDao
import com.codelabs.state.data.source.CalendarDataSource
import com.codelabs.state.domain.CompleteTaskUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * TaskRepository
 * 职责：数据的单一事实来源。协调本地数据库 (Room) 和 外部数据源 (Calendar) 以及 玩家状态 (UserStats)。
 */
interface TaskRepository {
    fun getAllTasks(): Flow<List<WellnessTask>>
    fun getUserStats(): Flow<UserStats> 
    suspend fun addTask(title: String, startTimeMillis: Long, endTimeMillis: Long, rrule: String?)
    suspend fun deleteTask(task: WellnessTask)
    suspend fun updateTask(task: WellnessTask)
    suspend fun completeTaskAndSync(task: WellnessTask) // 核心业务闭环
}

class DefaultTaskRepository(
    private val taskDao: WellnessTaskDao,
    private val userStatsDao: UserStatsDao,
    private val calendarDataSource: CalendarDataSource,
    private val completeTaskUseCase: CompleteTaskUseCase
) : TaskRepository {

    // 使用 Dao 中新的过滤查询
    override fun getAllTasks(): Flow<List<WellnessTask>> = taskDao.getAllActiveTasks()

    override fun getUserStats(): Flow<UserStats> = userStatsDao.getUserStatsFlow()
        .map { it ?: UserStats() }

    override suspend fun addTask(title: String, startTimeMillis: Long, endTimeMillis: Long, rrule: String?) {
        // 尝试写入日历，失败不影响本地保存
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
            calendarEventId = calendarEventId,
            expReward = 10, 
            goldReward = 50 
        )
        taskDao.insert(newTask)
    }

    override suspend fun deleteTask(task: WellnessTask) {
        // 安全地尝试删除日历事件
        task.calendarEventId?.let { eventId ->
            try {
                calendarDataSource.deleteEvent(eventId)
            } catch (e: Exception) {
                Log.e("TaskRepository", "Failed to delete calendar event: $eventId", e)
            }
        }
        taskDao.delete(task)
    }

    override suspend fun updateTask(task: WellnessTask) {
        taskDao.update(task)
    }

    // 实现核心业务闭环：安全更新
    override suspend fun completeTaskAndSync(task: WellnessTask) {
        try {
            // 1. 获取当前玩家状态（如果不存在则新建）
            val currentStats = userStatsDao.getUserStats() ?: UserStats()

            // 2. 调用领域层逻辑，计算新状态
            val newStats = completeTaskUseCase(currentStats, task.expReward, task.goldReward)

            // 3. 更新数据库：玩家状态
            userStatsDao.insertOrUpdate(newStats)

            // 4. 更新数据库：任务状态（标记为已完成）
            // 必须确保这一步执行成功，且不受日历操作影响
            taskDao.update(task.copy(checked = true))
            
            Log.d("TaskRepository", "Task ${task.id} marked as completed in DB. Stats updated.")

            // 5. 同步日历：打勾（独立包裹 try-catch）
            task.calendarEventId?.let { eventId ->
                try {
                    // 如果已经是 ✅ 开头就不重复加了
                    if (!task.label.startsWith("✅")) {
                        val success = calendarDataSource.updateEventTitle(eventId, "✅ ${task.label}")
                        if (success) {
                             Log.d("TaskRepository", "Calendar event $eventId title updated.")
                        } else {
                             Log.w("TaskRepository", "Calendar event $eventId not found or update failed.")
                        }
                    }
                } catch (e: Exception) {
                    // 日历同步失败仅记录日志，不回滚 DB
                    Log.e("TaskRepository", "Failed to sync calendar completion for event: $eventId", e)
                }
            }
        } catch (e: Exception) {
            // 只有当数据库操作本身失败时，才视为严重错误
             Log.e("TaskRepository", "Critical error completing task ${task.id}", e)
             throw e
        }
    }
}
