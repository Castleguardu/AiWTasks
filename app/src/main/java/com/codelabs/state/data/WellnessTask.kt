package com.codelabs.state.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wellness_tasks")
data class WellnessTask(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val label: String,
    val timeInMillis: Long,
    val rrule: String? = null,
    val checked: Boolean = false,
    val calendarEventId: Long? = null,
    // 新增游戏化奖励字段
    val expReward: Int = 10,   // 默认 10 经验
    val goldReward: Int = 50   // 默认 50 金币
)
