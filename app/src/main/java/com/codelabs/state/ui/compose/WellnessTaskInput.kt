package com.codelabs.state.ui.compose// WellnessTaskInput.kt
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.codelabs.state.data.RecurrenceType
import com.codelabs.state.utils.IntentUtils.addCalendarEvent
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellnessTaskInput(
    onTaskAddAndSync: (String, Long, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // --- 状态定义 ---
    var title by remember { mutableStateOf("") }

    // 时间状态：默认开始是现在，结束是1小时后
    var startTime by remember { mutableStateOf(Calendar.getInstance()) }
    var endTime by remember {
        mutableStateOf(Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) })
    }

    // 2. 定义一个 Key，用来强制重置 TimePicker
    var pickerKey by remember { mutableIntStateOf(0) }

    // 3. 标记当前是选 Start 还是 End
    var isSelectingStartTime by remember { mutableStateOf(true) }

    // 4. 根据当前模式，计算 TimePicker 应该显示的初始时间
    val initialHour = if (isSelectingStartTime) startTime.get(Calendar.HOUR_OF_DAY) else endTime.get(Calendar.HOUR_OF_DAY)
    val initialMinute = if (isSelectingStartTime) startTime.get(Calendar.MINUTE) else endTime.get(Calendar.MINUTE)

    // 5. ✅ 核心修改：使用 key() 包裹 rememberTimePickerState
    // 当 pickerKey 变化时，这个 State 会被销毁并重新创建，从而应用新的 initialHour
    val timePickerState = key(pickerKey) {
        rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )
    }

    // 控制弹窗显示
    var showTimePicker by remember { mutableStateOf(false) }

    // 重复规则状态
    var recurrenceType by remember { mutableStateOf(RecurrenceType.NONE) }
    var recurrenceInterval by remember { mutableStateOf("1") } // 间隔，默认为1
    var isDropdownExpanded by remember { mutableStateOf(false) } // 下拉菜单开关


    Column(modifier = modifier.padding(16.dp)) {

        // 1. 输入任务标题
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("任务标题") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. 时间显示与选择行
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = {
                    isSelectingStartTime = true
                    pickerKey++ // ✅ 关键：点击时改变 Key，强制 TimePicker 刷新初始值
                    showTimePicker = true
                }
            ) {
                Text("开始: ${formatTime(startTime)}")
            }

            Text("-")

            // 2. 结束时间按钮
            OutlinedButton(
                onClick = {
                    isSelectingStartTime = false
                    pickerKey++ // ✅ 关键：点击时改变 Key
                    showTimePicker = true
                }
            ) {
                Text("结束: ${formatTime(endTime)}")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. 重复规则行 (频率下拉 + 间隔输入)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 下拉菜单盒子
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { isDropdownExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(recurrenceType.label)
                }

                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    RecurrenceType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.label) },
                            onClick = {
                                recurrenceType = type
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // 如果选择了重复，显示间隔输入框
            if (recurrenceType != RecurrenceType.NONE) {
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = recurrenceInterval,
                    onValueChange = { if (it.all { char -> char.isDigit() }) recurrenceInterval = it },
                    label = { Text("每x(天/周)") },
                    modifier = Modifier.width(100.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. 同步按钮
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (title.isNotBlank()) {
                    // --- 核心逻辑：生成 RRULE ---
                    val rruleString = if (recurrenceType != RecurrenceType.NONE) {
                        // 格式：FREQ=WEEKLY;INTERVAL=2
                        val interval = recurrenceInterval.toIntOrNull() ?: 1
                        "FREQ=${recurrenceType.rruleValue};INTERVAL=$interval"
                    } else {
                        null
                    }

                    // 执行回调
                    onTaskAddAndSync(title, startTime.timeInMillis, rruleString)

                    // 唤起日历
                    addCalendarEvent(
                        context,
                        title,
                        startTime.timeInMillis,
                        endTime.timeInMillis,
                        rruleString
                    )

                    // 重置输入
                    title = ""
                }
            }
        ) {
            Text("添加到日历")
        }
    }

    // --- 弹窗组件 ---
    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // 保存时间
                    val targetCalendar = if (isSelectingStartTime) startTime else endTime
                    targetCalendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    targetCalendar.set(Calendar.MINUTE, timePickerState.minute)
                    // 触发 State 更新
                    if (isSelectingStartTime) startTime = targetCalendar.clone() as Calendar
                    else endTime = targetCalendar.clone() as Calendar

                    showTimePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("取消") }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

// 辅助函数：格式化时间显示
@SuppressLint("DefaultLocale")
fun formatTime(calendar: Calendar): String {
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    return String.format("%02d:%02d", hour, minute)
}

// 5. 辅助组件：因为 Material3 没有直接提供 TimePickerDialog，我们需要自己封装一个通用的
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)?,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = {
            // 这里放 TimePicker 的内容
            content()
        }
    )
}