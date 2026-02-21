package com.codelabs.state.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.codelabs.state.data.UserStats
import com.codelabs.state.data.WellnessTask
import com.codelabs.state.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TasksViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    // 1. 任务列表流：直接从数据库获取并保持为 StateFlow
    // 过滤掉已完成的任务（checked = false）
    val activeTasks: StateFlow<List<WellnessTask>> = repository.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 2. 玩家状态流：金币、等级、经验
    // 使用 UserStats? 类型，初始值为 null，避免显示虚假的默认值
    val userStats: StateFlow<UserStats?> = repository.getUserStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // 3. 完成任务意图
    fun onTaskCompleted(task: WellnessTask) {
        viewModelScope.launch {
            // 调用仓库层的业务闭环逻辑
            repository.completeTaskAndSync(task)
        }
    }

    // 4. 添加任务意图
    fun onTaskAdded(title: String, startTimeMillis: Long, rrule: String?) {
        viewModelScope.launch {
            repository.addTask(title, startTimeMillis, startTimeMillis + 3600000L, rrule)
        }
    }

    class Factory(private val repository: TaskRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TasksViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
