/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codelabs.state.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Pixel Art Theme Colors
private val PixelLightColorScheme = lightColorScheme(
    primary = PixelGreen,
    onPrimary = RetroBeige,
    primaryContainer = PixelGreen,
    onPrimaryContainer = RetroDarkBrown,
    secondary = PixelGold,
    onSecondary = RetroDarkBrown,
    secondaryContainer = PixelGold,
    onSecondaryContainer = RetroDarkBrown,
    background = RetroBeige,
    onBackground = RetroDarkBrown,
    surface = RetroBeige,
    onSurface = RetroDarkBrown,
    outline = RetroDarkBrown
)

private val PixelDarkColorScheme = darkColorScheme(
    primary = PixelGreen,
    onPrimary = RetroBeige,
    secondary = PixelGold,
    onSecondary = RetroDarkBrown,
    background = RetroDarkBrown, // Darker background for dark mode
    onBackground = RetroBeige,
    surface = RetroDarkBrown,
    onSurface = RetroBeige,
    outline = RetroBeige
)

@Composable
fun BasicStateCodelabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    // Disabled by default to enforce Pixel Art style
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> PixelDarkColorScheme
        else -> PixelLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
