package com.codelabs.state.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.codelabs.state.ui.theme.RetroBeige
import com.codelabs.state.ui.theme.RetroDarkBrown

/**
 * A custom container component with a retro pixel game style.
 * Features a beige background and a thick dark brown solid border.
 * No rounded corners or soft shadows.
 */
@Composable
fun PixelCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = RetroBeige,
    borderColor: Color = RetroDarkBrown,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(color = backgroundColor, shape = RectangleShape)
            .border(width = 3.dp, color = borderColor, shape = RectangleShape)
            .padding(16.dp) // Default internal padding
    ) {
        content()
    }
}
