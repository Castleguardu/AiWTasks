package com.codelabs.state.ui.compose

import com.codelabs.state.data.WellnessTask

data class RewardData(
    val coins: Int,
    val exp: Int,
    val task: WellnessTask // 将触发动画的任务本身也带上，方便回调时操作
)
