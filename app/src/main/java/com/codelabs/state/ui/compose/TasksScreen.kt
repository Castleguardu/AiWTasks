package com.codelabs.state.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codelabs.state.data.WellnessTask
import com.codelabs.state.ui.theme.PixelGold
import com.codelabs.state.ui.theme.PixelGreen
import com.codelabs.state.ui.theme.RetroDarkBrown
import com.codelabs.state.viewmodel.TasksViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TasksScreen(
    tasksViewModel: TasksViewModel = viewModel()
) {
    val tasks by tasksViewModel.activeTasks.collectAsState()
    val userStats by tasksViewModel.userStats.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // 顶部显示当前玩家状态 (可选，这里仅作演示，MainScreen 已包含)
        // ...

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items = tasks, key = { it.id }) { task ->
                TaskItem(
                    task = task,
                    onTaskChecked = { checked ->
                        if (checked) {
                            tasksViewModel.onTaskCompleted(task)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TaskItem(
    task: WellnessTask,
    onTaskChecked: (Boolean) -> Unit
) {
    // 动画状态：是否可见
    var isVisible by remember { mutableStateOf(true) }

    AnimatedVisibility(
        visible = isVisible,
        exit = fadeOut(animationSpec = tween(durationMillis = 300))
    ) {
        PixelCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
            ) {
                // 复选框
                Checkbox(
                    checked = task.checked,
                    onCheckedChange = { checked ->
                        if (checked) {
                            // 触发淡出动画，然后调用 ViewModel
                            isVisible = false
                            onTaskChecked(true)
                        }
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PixelGreen,
                        uncheckedColor = RetroDarkBrown,
                        checkmarkColor = RetroDarkBrown
                    )
                )

                Column(
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    // 标题
                    Text(
                        text = task.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = RetroDarkBrown,
                        textDecoration = if (task.checked) TextDecoration.LineThrough else null
                    )
                    
                    // 时间和奖励信息
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatDate(task.timeInMillis),
                            style = MaterialTheme.typography.bodySmall,
                            color = RetroDarkBrown.copy(alpha = 0.7f)
                        )

                        // 奖励显示：金色像素字
                        Text(
                            text = "💰${task.goldReward} | ✨${task.expReward}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PixelGold
                        )
                    }
                }
            }
        }
    }
}

private fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}
