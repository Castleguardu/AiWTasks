package com.codelabs.state.data.repository

import com.codelabs.state.data.WellnessTask
import com.codelabs.state.data.WellnessTaskDao
import com.codelabs.state.data.source.CalendarDataSource
import kotlinx.coroutines.flow.Flow

/**
 * TaskRepository
 * 职责：数据的单一事实来源。协调本地数据库 (Room) 和 外部数据源 (Calendar)。
 * 外部（ViewModel）只需要调用 repository.completeTask(task)，而不需要知道具体要操作哪些数据源。
 */
interface TaskRepository {
    fun getAllTasks(): Flow<List<WellnessTask>>
    suspend fun addTask(title: String, startTimeMillis: Long, endTimeMillis: Long, rrule: String?)
    suspend fun deleteTask(task: WellnessTask)
    suspend fun updateTask(task: WellnessTask)
    suspend fun toggleTaskCompleted(task: WellnessTask, completed: Boolean)
}

class DefaultTaskRepository(
    private val taskDao: WellnessTaskDao,
    private val calendarDataSource: CalendarDataSource
) : TaskRepository {

    override fun getAllTasks(): Flow<List<WellnessTask>> = taskDao.getAll()

    override suspend fun addTask(title: String, startTimeMillis: Long, endTimeMillis: Long, rrule: String?) {
        // 1. 先尝试向日历添加事件 (如果用户授权了)
        // 注意：这里我们做了一个策略选择，即使日历写入失败，依然写入本地数据库。
        val calendarEventId = calendarDataSource.addEvent(
            title = title,
            startMillis = startTimeMillis,
            endMillis = endTimeMillis,
            rrule = rrule
        )

        // 2. 将得到的 Calendar ID (可能为 null) 一起存入 Room
        val newTask = WellnessTask(
            label = title,
            timeInMillis = startTimeMillis,
            rrule = rrule,
            checked = false,
            calendarEventId = calendarEventId
        )
        taskDao.insert(newTask)
    }

    override suspend fun deleteTask(task: WellnessTask) {
        // 1. 如果有关联的日历事件，先删除日历事件
        task.calendarEventId?.let { eventId ->
            calendarDataSource.deleteEvent(eventId)
        }
        // 2. 删除本地数据库记录
        taskDao.delete(task)
    }

    override suspend fun updateTask(task: WellnessTask) {
        taskDao.update(task)
    }

    override suspend fun toggleTaskCompleted(task: WellnessTask, completed: Boolean) {
        // 1. 更新日历标题状态 (添加 ✅ 或移除)
        // 注意：这里我们封装了一个简单的逻辑，如果 completed 为 true，加 ✅，否则移除
        task.calendarEventId?.let { eventId ->
            val newTitle = if (completed) "✅ ${task.label}" else task.label.removePrefix("✅ ").trim()
            calendarDataSource.updateEventTitle(eventId, newTitle)
        }

        // 2. 更新本地数据库状态
        val updatedTask = task.copy(checked = completed)
        taskDao.update(updatedTask)
    }
}
