package com.codelabs.state.ui.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 通用的悬浮飘动特效组件
 *
 * @param visible 是否触发动画。当变为 true 时播放。
 * @param startOffsetY 初始 Y 轴偏移量 (px)，默认为 0
 * @param targetOffsetY 目标 Y 轴偏移量 (px)，默认为 -100f (向上飘)
 * @param durationMillis 动画总时长
 * @param content 要展示的飘动内容 (如 "+💰50")
 */
@Composable
fun FloatingEffect(
    visible: Boolean,
    modifier: Modifier = Modifier,
    startOffsetY: Float = 0f,
    targetOffsetY: Float = -100f,
    durationMillis: Int = 600,
    content: @Composable () -> Unit
) {
    if (!visible) return

    val offsetY = remember { Animatable(startOffsetY) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(visible) {
        // 重置状态
        offsetY.snapTo(startOffsetY)
        alpha.snapTo(1f)

        // 并行启动动画
        launch {
            offsetY.animateTo(
                targetValue = targetOffsetY,
                animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing)
            )
        }
        launch {
            // 后半段才开始透明度消失
            delay(durationMillis / 3L)
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = (durationMillis * 0.66).toInt())
            )
        }
    }

    Box(
        modifier = modifier
            .offset { IntOffset(0, offsetY.value.roundToInt()) }
            .alpha(alpha.value)
    ) {
        content()
    }
}
