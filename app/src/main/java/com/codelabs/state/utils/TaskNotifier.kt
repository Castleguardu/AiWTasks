package com.codelabs.state.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.codelabs.state.R

interface TaskNotifier {
    fun showTaskReminder(taskId: Int, taskTitle: String, rewardCoins: Int)
}

class TaskNotifierImpl(
    private val context: Context
) : TaskNotifier {

    companion object {
        const val CHANNEL_ID = "quest_start_channel"
        const val CHANNEL_NAME = "委托开始提醒"
        const val CHANNEL_DESC = "接收任务开始的通知"
        const val TAG = "TaskNotifier"
    }

    /**
     * 发送通知
     */
    override fun showTaskReminder(taskId: Int, taskTitle: String, rewardCoins: Int) {
        Log.d(TAG, "Attempting to show notification for task $taskId")

        // 1. 检查权限 (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "Notification permission NOT granted!")
                return
            }
        }

        // 2. 确保 Channel 已创建
        createNotificationChannel()

        // 3. 构建通知 (使用 alpha-only 图标)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pixel_sword) 
            .setContentTitle("⚔️ 【新委托开启】")
            .setContentText("勇者，任务「$taskTitle」现在开始！\n完成奖励: 💰$rewardCoins")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)

        // 4. 发送
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(taskId, builder.build())
                Log.d(TAG, "Notification posted successfully.")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException sending notification", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending notification", e)
        }
    }

    /**
     * 创建通知渠道 (Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
