package com.codelabs.state.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 商店商品实体
 */
@Entity(tableName = "reward_items")
data class RewardItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val cost: Int
)
