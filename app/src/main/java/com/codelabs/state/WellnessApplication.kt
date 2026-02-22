package com.codelabs.state

import android.app.Application
import com.codelabs.state.data.WellnessDatabase
import com.codelabs.state.data.repository.DefaultTaskRepository
import com.codelabs.state.data.repository.TaskRepository
import com.codelabs.state.data.source.AndroidCalendarDataSource
import com.codelabs.state.domain.CompleteTaskUseCase
import com.codelabs.state.utils.ReminderManager

class WellnessApplication : Application() {

    private val database: WellnessDatabase by lazy { WellnessDatabase.getDatabase(this) }

    private val calendarDataSource by lazy { AndroidCalendarDataSource(this) }

    private val completeTaskUseCase by lazy { CompleteTaskUseCase() }
    
    // 初始化 ReminderManager
    private val reminderManager by lazy { ReminderManager(this) }

    val taskRepository: TaskRepository by lazy {
        DefaultTaskRepository(
            taskDao = database.wellnessTaskDao(),
            userStatsDao = database.userStatsDao(),
            rewardItemDao = database.rewardItemDao(),
            calendarDataSource = calendarDataSource,
            completeTaskUseCase = completeTaskUseCase,
            reminderManager = reminderManager // 注入 ReminderManager
        )
    }
}
