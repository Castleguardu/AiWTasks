package com.codelabs.state.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 玩家状态实体
 * 存储等级、当前经验值、金币
 */
@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey
    val id: Int = 1, // 单例模式，ID 始终为 1
    val level: Int = 1,
    val currentExp: Int = 0,
    val gold: Int = 0
)
