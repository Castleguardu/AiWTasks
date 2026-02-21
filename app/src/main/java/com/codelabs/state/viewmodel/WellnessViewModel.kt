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

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.codelabs.state.WellnessApplication
import com.codelabs.state.data.WellnessTask
import com.codelabs.state.utils.IntentUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WellnessViewModel(application: Application) : AndroidViewModel(application) {
    
    private val dao = (application as WellnessApplication).database.wellnessTaskDao()

    // 这里的列表依然是 MutableStateList，它可以监听增删改
    private val _tasks = mutableStateListOf<WellnessTask>()
    val tasks: List<WellnessTask>
        get() = _tasks

    init {
        // 监听数据库变化，更新 UI
        viewModelScope.launch {
            dao.getAll().collect { list ->
                _tasks.clear()
                _tasks.addAll(list)
            }
        }
    }

    fun addTask(title: String, timeInMillis: Long, rrule: String?, calendarEventId: Long? = null) {
        viewModelScope.launch {
            val newTask = WellnessTask(
                label = title,
                timeInMillis = timeInMillis,
                rrule = rrule,
                checked = false,
                calendarEventId = calendarEventId
            )
            dao.insert(newTask)
        }
    }

    fun remove(item: WellnessTask) {
        viewModelScope.launch {
            // 如果任务有关联的日历事件，尝试删除
            if (item.calendarEventId != null) {
                // 在后台线程执行删除操作
                launch(Dispatchers.IO) {
                    IntentUtils.deleteTaskFromCalendar(getApplication(), item.calendarEventId)
                }
            }
            dao.delete(item)
        }
    }


    fun changeTaskChecked(item: WellnessTask, checked: Boolean) {
        viewModelScope.launch {
            if (checked && item.calendarEventId != null) {
                // 如果标记完成，更新日历标题
                 launch(Dispatchers.IO) {
                    IntentUtils.markTaskAsCompletedInCalendar(getApplication(), item.calendarEventId, item.label)
                }
            }
            dao.update(item.copy(checked = checked))
        }
    }
}
