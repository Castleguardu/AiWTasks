package com.codelabs.state.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "milestones")
data class Milestone(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val currentProgress: Int = 0,
    val maxProgress: Int = 10
)
