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
package com.codelabs.state.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codelabs.state.viewmodel.WellnessViewModel

@Composable
fun WellnessScreen(
    modifier: Modifier = Modifier,
    wellnessViewModel: WellnessViewModel = viewModel()
) {
    Column(modifier = modifier) {

        // 1. 顶部的输入区域 (我们刚才自定义的组件)
        WellnessTaskInput(
            onTaskAddAndSync = { title, time, rrule ->
                // 这里处理两个逻辑：

                // A. 更新 App 内部的列表显示
                // (注意：这里我们暂时忽略了 time 和 rrule，因为 App 内部列表还没做那么复杂)
                wellnessViewModel.addTask(title, time, rrule)

                // B. 日历同步逻辑已经在 WellnessTaskInput 内部通过 addCalendarEvent 触发了
                // 如果你想把逻辑提纯，也可以把 addCalendarEvent 移到这里调用
            }
        )

        // 2. 中间的任务列表 (Codelab 自带的列表)
        WellnessTasksList(
            list = wellnessViewModel.tasks,
            onCheckedTask = { task, checked ->
                wellnessViewModel.changeTaskChecked(task, checked)
            },
            onCloseTask = { task ->
                wellnessViewModel.remove(task)
            }
        )
    }
}
