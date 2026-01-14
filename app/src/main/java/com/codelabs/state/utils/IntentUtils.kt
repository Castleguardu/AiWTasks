package com.codelabs.state.utils

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast

object IntentUtils {
    fun addCalendarEvent(
        context: Context,
        title: String,
        beginTimeMillis: Long,
        endTimeMillis: Long, // ✅ 新增：结束时间
        rrule: String? = null
    ) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)

            // 传入开始和结束时间
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTimeMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTimeMillis)

            // 传入重复规则
            if (rrule != null) {
                putExtra(CalendarContract.Events.RRULE, rrule)
            }
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "未找到日历 App", Toast.LENGTH_SHORT).show()
        }
    }
}