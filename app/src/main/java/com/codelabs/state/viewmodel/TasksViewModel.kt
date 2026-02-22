package com.codelabs.state.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.codelabs.state.data.UserStats
import com.codelabs.state.data.WellnessTask
import com.codelabs.state.data.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TasksViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    // 1. 未完成任务流
    val activeTasks: StateFlow<List<WellnessTask>> = repository.getAllActiveTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 2. 已完成任务流（功勋档案）
    val completedTasks: StateFlow<List<WellnessTask>> = repository.getAllCompletedTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userStats: StateFlow<UserStats?> = repository.getUserStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun onTaskCompleted(task: WellnessTask) {
        viewModelScope.launch {
            repository.completeTaskAndSync(task)
        }
    }

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
