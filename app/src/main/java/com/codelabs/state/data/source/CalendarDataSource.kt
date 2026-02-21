package com.codelabs.state.data.source

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.TimeZone

/**
 * CalendarDataSource
 * 职责：封装所有 Android 系统日历的底层读写操作。
 * 不涉及任何业务逻辑（如经验值计算），仅负责 I/O。
 */
interface CalendarDataSource {
    suspend fun addEvent(title: String, startMillis: Long, endMillis: Long, rrule: String?): Long?
    suspend fun deleteEvent(eventId: Long): Boolean
    suspend fun updateEventTitle(eventId: Long, newTitle: String): Boolean
}

class AndroidCalendarDataSource(
    private val context: Context
) : CalendarDataSource {

    override suspend fun addEvent(title: String, startMillis: Long, endMillis: Long, rrule: String?): Long? {
        if (!hasPermission()) return null

        val calId = getPrimaryCalendarId() ?: return null

        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startMillis)
            put(CalendarContract.Events.DTEND, endMillis)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.CALENDAR_ID, calId)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            if (rrule != null) {
                put(CalendarContract.Events.RRULE, rrule)
            }
        }

        return try {
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            uri?.lastPathSegment?.toLongOrNull()
        } catch (e: Exception) {
            Log.e("CalendarDataSource", "Error inserting event", e)
            null
        }
    }

    override suspend fun deleteEvent(eventId: Long): Boolean {
        if (!hasPermission()) return false

        return try {
            val deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            val rows = context.contentResolver.delete(deleteUri, null, null)
            rows > 0
        } catch (e: Exception) {
            Log.e("CalendarDataSource", "Error deleting event $eventId", e)
            false
        }
    }

    override suspend fun updateEventTitle(eventId: Long, newTitle: String): Boolean {
        if (!hasPermission()) return false

        return try {
            val updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            val values = ContentValues().apply {
                put(CalendarContract.Events.TITLE, newTitle)
            }
            val rows = context.contentResolver.update(updateUri, values, null, null)
            rows > 0
        } catch (e: Exception) {
            Log.e("CalendarDataSource", "Error updating event $eventId", e)
            false
        }
    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getPrimaryCalendarId(): Long? {
        val projection = arrayOf(CalendarContract.Calendars._ID)
        
        // 1. Try Primary
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.IS_PRIMARY} = 1",
            null,
            null
        )?.use { if (it.moveToFirst()) return it.getLong(0) }
        
        // 2. Try Visible
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.VISIBLE} = 1",
            null,
            null
        )?.use { if (it.moveToFirst()) return it.getLong(0) }

        return null
    }
}
