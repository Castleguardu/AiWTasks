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
import com.codelabs.state.data.WellnessTask

class WellnessViewModel : ViewModel() {
    // 这里的列表依然是 MutableStateList，它可以监听增删改
    private val _tasks = ArrayList<WellnessTask>().toMutableStateList()
    val tasks: List<WellnessTask>
        get() = _tasks

    fun addTask(title: String, timeInMillis: Long, rrule: String?) {
        val newId = (_tasks.maxOfOrNull { it.id } ?: 0) + 1
        val newTask = WellnessTask(
            id = newId,
            label = title,
            timeInMillis = timeInMillis,
            rrule = rrule,
            checked = false
        )
        _tasks.add(newTask)
    }

    fun remove(item: WellnessTask) {
        _tasks.remove(item)
    }


    fun changeTaskChecked(item: WellnessTask, checked: Boolean) {
        // 1. 找到该任务在列表中的索引
        val index = _tasks.indexOfFirst { it.id == item.id }
        if (index != -1) {
            // 2. 创建一个 checked 状态改变了的新副本
            val newItem = _tasks[index].copy(checked = checked)
            // 3. 替换列表中的旧元素 -> 触发 Compose 重组
            _tasks[index] = newItem
        }
    }
}

