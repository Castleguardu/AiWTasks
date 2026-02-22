package com.codelabs.state.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codelabs.state.ui.theme.PixelGold
import com.codelabs.state.ui.theme.PixelGreen
import com.codelabs.state.ui.theme.RetroDarkBrown
import com.codelabs.state.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel()
) {
    val userStats by viewModel.userStats.collectAsState()
    val completedCount by viewModel.completedTasksCount.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 模块 A：角色档案
        item {
            PixelCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 占位头像 (64x64 矩形)
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.Gray)
                                .border(2.dp, RetroDarkBrown)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // 等级
                        Text(
                            text = "Lv. ${userStats?.level ?: 1}",
                            style = MaterialTheme.typography.displaySmall,
                            color = RetroDarkBrown
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 经验条
                    val exp = userStats?.currentExp ?: 0
                    val maxExp = 100 // 假设满级经验固定 100
                    
                    Text(
                        text = "EXP: $exp / $maxExp",
                        style = MaterialTheme.typography.labelSmall,
                        color = RetroDarkBrown,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    PixelProgressBar(
                        progress = exp / maxExp.toFloat(),
                        color = PixelGreen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                    )
                }
            }
        }

        // 模块 B：生涯统计
        item {
            PixelCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("生涯统计", style = MaterialTheme.typography.titleMedium, color = RetroDarkBrown)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    StatRow(icon = "💰", label = "历史累计获得金币", value = "${userStats?.gold ?: 0}")
                    StatRow(icon = "✅", label = "累计斩获任务", value = "$completedCount")
                }
            }
        }

        // 模块 C：长期羁绊与里程碑
        item {
            PixelCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("✨ 核心目标与羁绊", style = MaterialTheme.typography.titleMedium, color = RetroDarkBrown)
                    
                    MilestoneItem(
                        title = "独立开发上架 💻",
                        progressText = "60%",
                        progress = 0.6f,
                        color = PixelGold
                    )
                }
            }
        }
    }
}

@Composable
fun StatRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$icon $label", style = MaterialTheme.typography.bodyMedium, color = RetroDarkBrown)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = RetroDarkBrown, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    }
}

@Composable
fun MilestoneItem(title: String, progressText: String, progress: Float, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = RetroDarkBrown)
            Text(progressText, style = MaterialTheme.typography.labelSmall, color = RetroDarkBrown)
        }
        PixelProgressBar(
            progress = progress,
            color = color,
            modifier = Modifier.fillMaxWidth().height(16.dp)
        )
    }
}

/**
 * 像素风进度条
 * 使用 Canvas 绘制纯色填充和粗边框，不带圆角
 */
@Composable
fun PixelProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.LightGray.copy(alpha = 0.5f),
    borderColor: Color = RetroDarkBrown
) {
    Box(
        modifier = modifier
            .border(2.dp, borderColor, RectangleShape)
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxSize()
                .background(color)
        )
    }
}
