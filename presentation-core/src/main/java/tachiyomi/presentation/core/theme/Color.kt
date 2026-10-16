package tachiyomi.presentation.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalPrimaryTextColor = compositionLocalOf<Color?> { null }
val LocalSecondaryTextColor = compositionLocalOf<Color?> { null }

@Composable
@ReadOnlyComposable
fun primaryTextColor(): Color = LocalPrimaryTextColor.current ?: MaterialTheme.colorScheme.primary

@Composable
@ReadOnlyComposable
fun secondaryTextColor(): Color = LocalSecondaryTextColor.current ?: MaterialTheme.colorScheme.secondary

val ColorScheme.active: Color
    @Composable
    get() {
        return if (isSystemInDarkTheme()) Color(255, 235, 59) else Color(255, 193, 7)
    }
