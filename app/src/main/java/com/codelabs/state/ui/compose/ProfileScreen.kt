package com.codelabs.state.ui.compose

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.codelabs.state.data.Milestone
import com.codelabs.state.ui.theme.PixelGold
import com.codelabs.state.ui.theme.PixelGreen
import com.codelabs.state.ui.theme.RetroBeige
import com.codelabs.state.ui.theme.RetroDarkBrown
import com.codelabs.state.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel()
) {
    val userStats by viewModel.userStats.collectAsState()
    val completedCount by viewModel.completedTasksCount.collectAsState()
    val avatarFile by viewModel.avatarFile.collectAsState()
    val milestones by viewModel.milestones.collectAsState()

    var showNameDialog by remember { mutableStateOf(false) }
    var showMilestoneDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onAvatarSelected(uri)
        }
    }

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
                        // 头像框
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.Gray)
                                .border(2.dp, RetroDarkBrown)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarFile != null) {
                                AsyncImage(
                                    model = avatarFile,
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text("?", color = RetroDarkBrown)
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            // 名字与编辑
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userStats?.userName ?: "勇者",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = RetroDarkBrown
                                )
                                IconButton(onClick = { showNameDialog = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Name", tint = RetroDarkBrown, modifier = Modifier.size(16.dp))
                                }
                            }
                            
                            // 等级
                            Text(
                                text = "Lv. ${userStats?.level ?: 1}",
                                style = MaterialTheme.typography.displaySmall,
                                color = RetroDarkBrown
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 经验条
                    val exp = userStats?.currentExp ?: 0
                    val maxExp = 100 
                    
                    Text(
                        text = "EXP: $exp / $maxExp",
                        style = MaterialTheme.typography.labelSmall,
                        color = RetroDarkBrown,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    PixelProgressBar(
                        progress = exp / maxExp.toFloat(),
                        color = PixelGreen,
                        modifier = Modifier.fillMaxWidth().height(24.dp)
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✨ 核心目标与羁绊", style = MaterialTheme.typography.titleMedium, color = RetroDarkBrown)
                        IconButton(
                            onClick = { showMilestoneDialog = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Milestone", tint = RetroDarkBrown)
                        }
                    }
                    
                    if (milestones.isEmpty()) {
                        Text("暂无目标，快去添加吧！", style = MaterialTheme.typography.bodySmall, color = RetroDarkBrown.copy(alpha = 0.5f))
                    } else {
                        // 动态列表
                        milestones.forEach { milestone ->
                            MilestoneItem(
                                title = milestone.title,
                                progressText = "${milestone.currentProgress}/${milestone.maxProgress}",
                                progress = milestone.currentProgress.toFloat() / milestone.maxProgress.toFloat(),
                                color = PixelGold,
                                onIncrement = { viewModel.incrementMilestoneProgress(milestone) },
                                isComplete = milestone.currentProgress >= milestone.maxProgress
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showNameDialog) {
        EditNameDialog(
            currentName = userStats?.userName ?: "",
            onDismiss = { showNameDialog = false },
            onConfirm = { 
                viewModel.updateUserName(it)
                showNameDialog = false
            }
        )
    }

    if (showMilestoneDialog) {
        AddMilestoneDialog(
            onDismiss = { showMilestoneDialog = false },
            onConfirm = { title, max ->
                viewModel.addMilestone(title, max)
                showMilestoneDialog = false
            }
        )
    }
}

@Composable
fun MilestoneItem(
    title: String, 
    progressText: String, 
    progress: Float, 
    color: Color, 
    onIncrement: () -> Unit,
    isComplete: Boolean
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = RetroDarkBrown)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isComplete) "达成！" else progressText, 
                    style = MaterialTheme.typography.labelSmall, 
                    color = if (isComplete) Color.Red else RetroDarkBrown
                )
                if (!isComplete) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(PixelGreen, RoundedCornerShape(4.dp))
                            .clickable { onIncrement() }
                            .border(1.dp, RetroDarkBrown, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increment", tint = RetroDarkBrown, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
        PixelProgressBar(
            progress = progress,
            color = if (isComplete) Color.Red else color, // 达成变红
            modifier = Modifier.fillMaxWidth().height(16.dp)
        )
    }
}

@Composable
fun StatRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$icon $label", style = MaterialTheme.typography.bodyMedium, color = RetroDarkBrown)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = RetroDarkBrown, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EditNameDialog(currentName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf(currentName) }
    Dialog(onDismissRequest = onDismiss) {
        PixelCard(modifier = Modifier.padding(16.dp)) {
            Column {
                Text("修改名字", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { if (name.isNotBlank()) onConfirm(name) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = PixelGreen)
                ) {
                    Text("保存", color = RetroDarkBrown)
                }
            }
        }
    }
}

@Composable
fun AddMilestoneDialog(onDismiss: () -> Unit, onConfirm: (String, Int) -> Unit) {
    var title by remember { mutableStateOf("") }
    var maxStr by remember { mutableStateOf("10") }
    Dialog(onDismissRequest = onDismiss) {
        PixelCard(modifier = Modifier.padding(16.dp)) {
            Column {
                Text("新建羁绊/目标", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title, 
                    onValueChange = { title = it }, 
                    label = { Text("目标名称") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = maxStr, 
                    onValueChange = { if (it.all { c -> c.isDigit() }) maxStr = it }, 
                    label = { Text("总进度值") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        val max = maxStr.toIntOrNull() ?: 10
                        if (title.isNotBlank() && max > 0) onConfirm(title, max) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = PixelGreen)
                ) {
                    Text("创建", color = RetroDarkBrown)
                }
            }
        }
    }
}

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
