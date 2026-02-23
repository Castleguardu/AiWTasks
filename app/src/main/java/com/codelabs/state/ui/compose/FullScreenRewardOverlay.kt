package com.codelabs.state.ui.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codelabs.state.ui.theme.PixelGold
import com.codelabs.state.ui.theme.RetroBeige
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FullScreenRewardOverlay(
    reward: RewardData,
    onAnimationEnd: () -> Unit
) {
    // 动画状态
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(1f) } // 文字内容的透明度，用于离场

    LaunchedEffect(Unit) {
        // 1. 进场：背景变黑，文字弹簧弹出
        launch {
            alpha.animateTo(0.6f, animationSpec = tween(300))
        }
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioHighBouncy, // 高回弹
                    stiffness = Spring.StiffnessMedium
                )
            )
        }

        // 2. 停留展示
        delay(1500)

        // 3. 离场：文字淡出，背景淡出
        launch {
            contentAlpha.animateTo(0f, animationSpec = tween(300))
        }
        launch {
            delay(100) // 背景稍微晚一点消失
            alpha.animateTo(0f, animationSpec = tween(300))
        }
        
        // 等待动画完全结束
        delay(300)
        
        // 4. 回调
        onAnimationEnd()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = alpha.value)) // 半透明遮罩
            .clickable(enabled = false) {}, // 拦截点击，防止透传
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(contentAlpha.value)
        ) {
            Text(
                text = "✨ 委托完成！ ✨",
                style = MaterialTheme.typography.displaySmall,
                color = RetroBeige,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "💰 +${reward.coins}   ✨ +${reward.exp}",
                style = MaterialTheme.typography.displayMedium, // 巨大字体
                color = PixelGold,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}
