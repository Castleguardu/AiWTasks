package com.codelabs.state.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.codelabs.state.utils.TaskNotifierImpl

class TaskReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("TaskReminderWorker", "Worker started.")
        
        // 读取参数
        val taskId = inputData.getInt("TASK_ID", -1)
        val taskTitle = inputData.getString("TASK_TITLE") ?: "未知任务"
        val rewardCoins = inputData.getInt("REWARD_COINS", 0)

        Log.d("TaskReminderWorker", "Input: id=$taskId, title=$taskTitle")

        if (taskId == -1) {
            Log.e("TaskReminderWorker", "Invalid task ID, worker failed.")
            return Result.failure()
        }

        // 发送通知
        val notifier = TaskNotifierImpl(applicationContext)
        notifier.showTaskReminder(taskId, taskTitle, rewardCoins)

        Log.d("TaskReminderWorker", "Worker finished success.")
        return Result.success()
    }
}
