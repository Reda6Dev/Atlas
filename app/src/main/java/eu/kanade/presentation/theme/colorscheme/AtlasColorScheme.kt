package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

internal object AtlasColorScheme : BaseColorScheme() {

    private val primary = Color(0xFF4CC9F0)

    override val darkScheme = darkColorScheme(
        primary = primary,
        onPrimary = Color(0xFF06223A),
        primaryContainer = Color(0xFF17496A),
        onPrimaryContainer = Color(0xFFEAF0FF),
        inversePrimary = Color(0xFF0A6F96),
        secondary = primary,
        onSecondary = Color(0xFF06223A),
        secondaryContainer = primary.copy(alpha = 0.16f).compositeOver(Color(0xFF131D3B)),
        onSecondaryContainer = primary,
        tertiary = Color(0xFF7EE787),
        onTertiary = Color(0xFF06223A),
        tertiaryContainer = Color(0xFF17496A),
        onTertiaryContainer = Color(0xFFEAF0FF),
        background = Color(0xFF0A1128),
        onBackground = Color(0xFFEAF0FF),
        surface = Color(0xFF131D3B),
        onSurface = Color(0xFFEAF0FF),
        surfaceVariant = Color(0xFF18244A),
        onSurfaceVariant = Color(0xFF9AA8CC),
        surfaceTint = primary,
        inverseSurface = Color(0xFFEAF0FF),
        inverseOnSurface = Color(0xFF0A1128),
        error = Color(0xFFFF7A93),
        onError = Color(0xFF3A0A18),
        errorContainer = Color(0xFFF0698A),
        onErrorContainer = Color(0xFF3A0A18),
        outline = Color(0xFF22305A),
        outlineVariant = Color(0xFF22305A),
        surfaceContainerLowest = Color(0xFF0A1128),
        surfaceContainerLow = Color(0xFF131D3B),
        surfaceContainer = Color(0xFF18244A),
        surfaceContainerHigh = Color(0xFF18244A),
        surfaceContainerHighest = Color(0xFF22305A),
    )

    override val lightScheme = lightColorScheme(
        primary = primary,
        onPrimary = Color(0xFF06223A),
        primaryContainer = Color(0xFFB5E8F7),
        onPrimaryContainer = Color(0xFF06223A),
        inversePrimary = Color(0xFF0A6F96),
        secondary = primary,
        onSecondary = Color(0xFF06223A),
        secondaryContainer = primary.copy(alpha = 0.16f).compositeOver(Color.White),
        onSecondaryContainer = Color(0xFF0A6F96),
        tertiary = Color(0xFF267A42),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFB5E8F7),
        onTertiaryContainer = Color(0xFF06223A),
        background = Color(0xFFF2F5FB),
        onBackground = Color(0xFF0E1A33),
        surface = Color.White,
        onSurface = Color(0xFF0E1A33),
        surfaceVariant = Color.White,
        onSurfaceVariant = Color(0xFF55627F),
        surfaceTint = primary,
        inverseSurface = Color(0xFF0E1A33),
        inverseOnSurface = Color(0xFFF2F5FB),
        error = Color(0xFFC2254A),
        onError = Color.White,
        errorContainer = Color(0xFFD63A60),
        onErrorContainer = Color.White,
        outline = Color(0xFFD5DDEE),
        outlineVariant = Color(0xFFD5DDEE),
        surfaceContainerLowest = Color(0xFFF2F5FB),
        surfaceContainerLow = Color.White,
        surfaceContainer = Color.White,
        surfaceContainerHigh = Color.White,
        surfaceContainerHighest = Color.White,
    )
}
