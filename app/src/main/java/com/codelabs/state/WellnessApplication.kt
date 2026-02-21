package com.codelabs.state

import android.app.Application
import com.codelabs.state.data.WellnessDatabase
import com.codelabs.state.data.repository.DefaultTaskRepository
import com.codelabs.state.data.repository.TaskRepository
import com.codelabs.state.data.source.AndroidCalendarDataSource

class WellnessApplication : Application() {

    private val database: WellnessDatabase by lazy { WellnessDatabase.getDatabase(this) }

    // 1. 初始化 Calendar 数据源 (依赖 Context)
    private val calendarDataSource by lazy { AndroidCalendarDataSource(this) }

    // 2. 将 Dao 和 DataSource 注入到 Repository
    val taskRepository: TaskRepository by lazy {
        DefaultTaskRepository(
            taskDao = database.wellnessTaskDao(),
            calendarDataSource = calendarDataSource
        )
    }
}
