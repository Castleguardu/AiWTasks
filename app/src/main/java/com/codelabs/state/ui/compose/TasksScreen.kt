package com.codelabs.state.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codelabs.state.data.WellnessTask
import com.codelabs.state.ui.theme.PixelGold
import com.codelabs.state.ui.theme.PixelGreen
import com.codelabs.state.ui.theme.RetroBeige
import com.codelabs.state.ui.theme.RetroDarkBrown
import com.codelabs.state.viewmodel.TasksViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TasksScreen(
    tasksViewModel: TasksViewModel = viewModel()
) {
    val activeTasks by tasksViewModel.activeTasks.collectAsState()
    val completedTasks by tasksViewModel.completedTasks.collectAsState()
    
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val titles = listOf("当前委托", "功勋档案")

    // --- 全屏动画状态 ---
    var rewardEvent by remember { mutableStateOf<RewardData?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // TabRow
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = RetroBeige,
                contentColor = RetroDarkBrown,
                indicator = { tabPositions ->
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                            .height(4.dp)
                            .background(PixelGold)
                    )
                },
                divider = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(RetroDarkBrown)
                    )
                }
            ) {
                titles.forEachIndexed { index, title ->
                    val selected = pagerState.currentPage == index
                    Tab(
                        selected = selected,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (selected) RetroDarkBrown else RetroDarkBrown.copy(alpha = 0.5f),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                if (page == 0) {
                    // Page 0: Active Tasks
                    ActiveTasksList(
                        tasks = activeTasks,
                        onTaskCompleted = { task ->
                            // 拦截：不直接调用 VM，而是先设置动画数据
                            rewardEvent = RewardData(
                                coins = task.goldReward,
                                exp = task.expReward,
                                task = task
                            )
                        }
                    )
                } else {
                    // Page 1: Completed Tasks
                    CompletedTasksList(
                        tasks = completedTasks
                    )
                }
            }
        }

        // --- 全屏动画遮罩 (位于最顶层) ---
        rewardEvent?.let { data ->
            FullScreenRewardOverlay(
                reward = data,
                onAnimationEnd = {
                    // 动画结束：真正提交数据
                    tasksViewModel.onTaskCompleted(data.task)
                    // 重置状态
                    rewardEvent = null
                }
            )
        }
    }
}

@Composable
fun ActiveTasksList(
    tasks: List<WellnessTask>,
    onTaskCompleted: (WellnessTask) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = tasks, key = { it.id }) { task ->
            TaskItem(
                task = task,
                onTaskChecked = { checked ->
                    if (checked) {
                        onTaskCompleted(task)
                    }
                }
            )
        }
    }
}

@Composable
fun CompletedTasksList(
    tasks: List<WellnessTask>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = tasks, key = { it.id }) { task ->
            CompletedTaskItem(task = task)
        }
    }
}

@Composable
fun TaskItem(
    task: WellnessTask,
    onTaskChecked: (Boolean) -> Unit
) {
    // 移除了局部的 FloatingEffect 逻辑，简化为纯状态展示
    // 点击 Checkbox 直接触发回调
    
    PixelCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Checkbox(
                checked = task.checked,
                onCheckedChange = { checked ->
                    if (checked) {
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
                Text(
                    text = task.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = RetroDarkBrown
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDate(task.timeInMillis),
                        style = MaterialTheme.typography.bodySmall,
                        color = RetroDarkBrown.copy(alpha = 0.7f)
                    )

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

@Composable
fun CompletedTaskItem(
    task: WellnessTask
) {
    Box {
        PixelCard(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0.7f),
            backgroundColor = RetroBeige.copy(alpha = 0.5f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Text(
                        text = task.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = RetroDarkBrown.copy(alpha = 0.6f),
                        textDecoration = TextDecoration.LineThrough
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "完成时间: ${formatDate(System.currentTimeMillis())}",
                        style = MaterialTheme.typography.bodySmall,
                        color = RetroDarkBrown.copy(alpha = 0.5f)
                    )
                }
            }
        }
        
        Text(
            text = "CLEARED",
            color = Color.Red.copy(alpha = 0.4f),
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
                .rotate(-15f)
                .border(2.dp, Color.Red.copy(alpha = 0.4f), RectangleShape)
                .padding(horizontal = 4.dp)
        )
    }
}

private fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}
