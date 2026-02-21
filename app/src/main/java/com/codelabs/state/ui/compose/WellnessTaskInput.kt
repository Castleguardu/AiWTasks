package com.codelabs.state.ui.compose

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
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
import androidx.core.content.ContextCompat
import com.codelabs.state.data.RecurrenceType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellnessTaskInput(
    onTaskAdd: (String, Long, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // --- 状态定义 ---
    var title by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf(Calendar.getInstance()) }
    var endTime by remember {
        mutableStateOf(Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) })
    }
    var pickerKey by remember { mutableIntStateOf(0) }
    var isSelectingStartTime by remember { mutableStateOf(true) }

    val initialHour = if (isSelectingStartTime) startTime.get(Calendar.HOUR_OF_DAY) else endTime.get(Calendar.HOUR_OF_DAY)
    val initialMinute = if (isSelectingStartTime) startTime.get(Calendar.MINUTE) else endTime.get(Calendar.MINUTE)

    val timePickerState = key(pickerKey) {
        rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = true)
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startTime.timeInMillis)
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var showTimePicker by remember { mutableStateOf(false) }

    var recurrenceType by remember { mutableStateOf(RecurrenceType.NONE) }
    var recurrenceInterval by remember { mutableStateOf("1") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val granted = permissions[Manifest.permission.WRITE_CALENDAR] == true
            if (granted) {
                Toast.makeText(context, "日历权限已获取，请再次点击添加", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "权限被拒绝，任务将仅保存到本地，不同步日历", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Column(modifier = modifier.padding(16.dp)) {

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("任务标题") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Date Selection
        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("日期: ${dateFormatter.format(startTime.time)}")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Time Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = {
                    isSelectingStartTime = true
                    pickerKey++
                    showTimePicker = true
                }
            ) {
                Text("开始: ${formatTime(startTime)}")
            }

            Text("-")

            OutlinedButton(
                onClick = {
                    isSelectingStartTime = false
                    pickerKey++
                    showTimePicker = true
                }
            ) {
                Text("结束: ${formatTime(endTime)}")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Recurrence
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            if (recurrenceType != RecurrenceType.NONE) {
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = recurrenceInterval,
                    onValueChange = { if (it.all { char -> char.isDigit() }) recurrenceInterval = it },
                    label = { Text("间隔") },
                    modifier = Modifier.width(80.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (title.isNotBlank()) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.WRITE_CALENDAR
                    ) == PackageManager.PERMISSION_GRANTED

                    if (!hasPermission) {
                        calendarPermissionLauncher.launch(
                            arrayOf(Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR)
                        )
                    } else {
                        val rruleString = if (recurrenceType != RecurrenceType.NONE) {
                            val interval = recurrenceInterval.toIntOrNull() ?: 1
                            "FREQ=${recurrenceType.rruleValue};INTERVAL=$interval"
                        } else {
                            null
                        }
                        
                        onTaskAdd(title, startTime.timeInMillis, rruleString)
                        
                        title = ""
                    }
                }
            }
        ) {
            Text("添加到日历")
        }
    }
    
    if (showTimePicker) {
        CustomTimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val targetCalendar = if (isSelectingStartTime) startTime else endTime
                    targetCalendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    targetCalendar.set(Calendar.MINUTE, timePickerState.minute)
                    if (isSelectingStartTime) startTime = targetCalendar.clone() as Calendar
                    else endTime = targetCalendar.clone() as Calendar
                    showTimePicker = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("取消") } }
        ) { TimePicker(state = timePickerState) }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            utcCalendar.timeInMillis = millis
                            val year = utcCalendar.get(Calendar.YEAR)
                            val month = utcCalendar.get(Calendar.MONTH)
                            val day = utcCalendar.get(Calendar.DAY_OF_MONTH)
                            
                            val newStart = startTime.clone() as Calendar
                            newStart.set(year, month, day)
                            startTime = newStart
                            
                            val newEnd = endTime.clone() as Calendar
                            newEnd.set(year, month, day)
                            endTime = newEnd
                        }
                        showDatePicker = false
                    }
                ) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }
        ) { DatePicker(state = datePickerState) }
    }
}

@SuppressLint("DefaultLocale")
fun formatTime(calendar: Calendar): String {
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    return String.format("%02d:%02d", hour, minute)
}

@Composable
fun CustomTimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)?,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = { content() }
    )
}
