package com.codelabs.state.utils

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.util.TimeZone

object IntentUtils {
    
    /**
     * 直接向系统日历插入事件，并返回事件ID
     * 需要 WRITE_CALENDAR 权限
     */
    fun addCalendarEvent(
        context: Context,
        title: String,
        beginTimeMillis: Long,
        endTimeMillis: Long,
        rrule: String? = null
    ): Long? {
        // 检查权限
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "缺少日历权限，无法自动添加", Toast.LENGTH_SHORT).show()
            // 如果没有权限，仍然可以使用 Intent 方式让用户手动添加，但这无法返回 ID
            // 这里我们仅做简单的 Toast 提示，实际业务中应该在 UI 层请求权限
            return null
        }

        // 获取主日历 ID
        val calId = getPrimaryCalendarId(context)
        if (calId == -1L) {
            Toast.makeText(context, "未找到可用日历账户", Toast.LENGTH_SHORT).show()
            return null
        }

        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, beginTimeMillis)
            put(CalendarContract.Events.DTEND, endTimeMillis)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.CALENDAR_ID, calId)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            if (rrule != null) {
                put(CalendarContract.Events.RRULE, rrule)
            }
        }

        return try {
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            // content://com.android.calendar/events/123 -> 123
            uri?.lastPathSegment?.toLongOrNull()
        } catch (e: Exception) {
            Log.e("IntentUtils", "Error inserting event", e)
            Toast.makeText(context, "添加日历事件失败", Toast.LENGTH_SHORT).show()
            null
        }
    }

    /**
     * 查找第一个可用的主日历 ID
     */
    private fun getPrimaryCalendarId(context: Context): Long {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
        )
        
        // 尝试获取 primary calendar
        // 注意：有些设备可能没有标记为 PRIMARY 的日历，这里做一个简单的查找逻辑
        // 优先找 IS_PRIMARY = 1，如果没有，就取第一个可见且有拥有者账户的日历
        
        // 1. 尝试查找 IS_PRIMARY
        var cursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.IS_PRIMARY} = 1",
            null,
            null
        )
        
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getLong(0)
            }
        }
        
        // 2. 如果没找到，尝试查找任意可见日历
         cursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.VISIBLE} = 1",
            null,
            null
        )
        
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getLong(0)
            }
        }

        return -1
    }

    /**
     * 从系统日历中删除指定 ID 的事件
     * 需要 WRITE_CALENDAR 权限
     */
    fun deleteTaskFromCalendar(context: Context, eventId: Long) {
        if (eventId < 0) return
        
        // 简单检查权限，虽然调用方通常在后台线程，但这可以防止崩溃
         if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
             Log.w("IntentUtils", "Missing permissions to delete event $eventId")
             return
         }

        try {
            val deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            val rows = context.contentResolver.delete(deleteUri, null, null)
            if (rows > 0) {
                Log.d("IntentUtils", "Deleted calendar event ID: $eventId")
            } else {
                Log.w("IntentUtils", "Calendar event ID: $eventId not found or not deleted")
            }
        } catch (e: Exception) {
            Log.e("IntentUtils", "Error deleting calendar event", e)
        }
    }

    /**
     * 更新系统日历中的事件，在其标题前加上‘✅’符号
     * 需要 WRITE_CALENDAR 权限
     */
    fun markTaskAsCompletedInCalendar(context: Context, eventId: Long, title: String) {
        if (eventId < 0) return
        
         if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
             Log.w("IntentUtils", "Missing permissions to update event $eventId")
             return
         }

        try {
            val updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            val values = ContentValues().apply {
                put(CalendarContract.Events.TITLE, "✅ $title")
            }
            val rows = context.contentResolver.update(updateUri, values, null, null)
            if (rows > 0) {
                Log.d("IntentUtils", "Marked calendar event ID: $eventId as completed")
            } else {
                Log.w("IntentUtils", "Calendar event ID: $eventId not found or not updated")
            }
        } catch (e: Exception) {
            Log.e("IntentUtils", "Error updating calendar event", e)
        }
    }
}
