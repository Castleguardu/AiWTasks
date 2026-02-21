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
    // ViewModel 默认值需要根据上下文提供，但在 MainScreen 中我们已经注入了，这里可以不传默认值或者用 @Preview 做特定处理
    // 为了兼容 MainScreen 的调用，我们移除这里的默认值，强制从外部传入，或者使用 hiltViewModel (如果引入了 Hilt)
    // 鉴于 Codelab 现状，我们保留参数，并在预览时提供 Mock
    wellnessViewModel: WellnessViewModel
) {
    Column(modifier = modifier) {

        // 1. 顶部的输入区域
        WellnessTaskInput(
            // 回调签名已更新：(String, Long, String?) -> Unit
            onTaskAdd = { title, time, rrule ->
                // UI 层只负责把意图传递给 ViewModel
                // ViewModel 会调用 Repository，Repository 会处理 Room 和 Calendar 的写入细节
                wellnessViewModel.addTask(title, time, rrule)
            }
        )

        // 2. 中间的任务列表
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
