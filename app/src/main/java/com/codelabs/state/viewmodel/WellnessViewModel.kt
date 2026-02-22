/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codelabs.state.viewmodel

import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.codelabs.state.data.WellnessTask
import com.codelabs.state.data.repository.TaskRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class WellnessViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    // 使用 MutableStateList 以便 Compose 能够高效地重组列表项
    // 虽然 StateFlow 是更推荐的架构，但在目前的 Codelab 基础上保持简单迁移是合理的
    private val _tasks = ArrayList<WellnessTask>().toMutableStateList()
    val tasks: List<WellnessTask>
        get() = _tasks

    init {
        // 监听 Repository 数据流，自动更新 UI 状态
        viewModelScope.launch {
            // TaskRepository 接口已经修改，getAllTasks() 已更名为 getAllActiveTasks()
            repository.getAllActiveTasks().collect { list ->
                _tasks.clear()
                _tasks.addAll(list)
            }
        }
    }

    /**
     * 添加任务
     * ViewModel 不关心具体怎么存到 DB 或日历，只负责转发意图。
     */
    fun addTask(title: String, timeInMillis: Long, rrule: String?) {
        viewModelScope.launch {
            // 这里我们假设默认结束时间是开始时间 + 1小时，后续可以从 UI 传入
            val endTimeMillis = timeInMillis + 3600000L 
            repository.addTask(title, timeInMillis, endTimeMillis, rrule)
        }
    }

    /**
     * 删除任务
     */
    fun remove(item: WellnessTask) {
        viewModelScope.launch {
            repository.deleteTask(item)
        }
    }

    /**
     * 切换任务完成状态
     */
    fun changeTaskChecked(item: WellnessTask, checked: Boolean) {
        viewModelScope.launch {
            if (checked) {
                repository.completeTaskAndSync(item)
            } else {
                repository.updateTask(item.copy(checked = false))
            }
        }
    }

    // Factory 用于创建带参数的 ViewModel
    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: TaskRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WellnessViewModel::class.java)) {
                return WellnessViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
