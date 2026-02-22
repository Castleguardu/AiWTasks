package com.codelabs.state.utils

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.codelabs.state.data.WellnessTask
import com.codelabs.state.worker.TaskReminderWorker
import java.util.concurrent.TimeUnit

class ReminderManager(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    /**
     * 调度任务开始提醒
     * @param task 任务实体，必须包含 startTimeMillis
     */
    fun scheduleReminder(task: WellnessTask) {
        val currentTime = System.currentTimeMillis()
        val startTime = task.timeInMillis
        
        // 目标触发时间：任务开始前 1 分钟
        // 例如：任务 10:00 开始，我们希望 09:59 提醒
        val triggerTime = startTime - 1 * 60 * 1000 
        
        // 计算延迟时间
        val delay = triggerTime - currentTime

        Log.d("ReminderManager", "Scheduling reminder for task ${task.id}: start=$startTime, trigger=$triggerTime, delay=$delay ms")

        // 过滤无效调度：如果 delay <= 0，说明现在已经过了提醒时间（或者任务马上开始），不调度 WorkManager
        if (delay > 0) {
            val data = Data.Builder()
                .putInt("TASK_ID", task.id)
                .putString("TASK_TITLE", task.label)
                .putInt("REWARD_COINS", task.goldReward)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("task_${task.id}") // Tag 用于后续取消
                .build()

            // 使用 REPLACE 策略，确保同一任务 ID 只有一个待执行的提醒
            workManager.enqueueUniqueWork(
                "reminder_${task.id}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            Log.d("ReminderManager", "Reminder scheduled successfully.")
        } else {
            Log.w("ReminderManager", "Skipping reminder: time has passed or is too close.")
        }
    }

    /**
     * 取消提醒（当任务完成或删除时调用）
     */
    fun cancelReminder(taskId: Int) {
        workManager.cancelAllWorkByTag("task_$taskId")
        Log.d("ReminderManager", "Reminder canceled for task $taskId")
    }
}
