package com.codelabs.state.data



data class WellnessTask(
    val id: Int,
    val label: String,
    val timeInMillis: Long,   // 任务时间
    val rrule: String? = null,// 重复规则
    val checked: Boolean = false // ✅ 直接用 Boolean，不再用 MutableState
)