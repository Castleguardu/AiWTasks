package com.codelabs.state

import android.app.Application
import com.codelabs.state.data.WellnessDatabase
import com.codelabs.state.data.repository.DefaultTaskRepository
import com.codelabs.state.data.repository.TaskRepository
import com.codelabs.state.data.source.AndroidCalendarDataSource
import com.codelabs.state.domain.CompleteTaskUseCase

class WellnessApplication : Application() {

    private val database: WellnessDatabase by lazy { WellnessDatabase.getDatabase(this) }

    // 1. 初始化 Calendar 数据源 (依赖 Context)
    private val calendarDataSource by lazy { AndroidCalendarDataSource(this) }

    // 2. 初始化 UseCase (纯业务逻辑，无依赖)
    private val completeTaskUseCase by lazy { CompleteTaskUseCase() }

    // 3. 将 Dao, DataSource 和 UseCase 注入到 Repository
    val taskRepository: TaskRepository by lazy {
        DefaultTaskRepository(
            taskDao = database.wellnessTaskDao(),
            userStatsDao = database.userStatsDao(), // 新增注入
            calendarDataSource = calendarDataSource,
            completeTaskUseCase = completeTaskUseCase // 新增注入
        )
    }
}
