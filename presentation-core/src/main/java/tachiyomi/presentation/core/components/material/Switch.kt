package tachiyomi.presentation.core.components.material

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Switch as MaterialSwitch

@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val disabledAlpha = 0.38f
    MaterialSwitch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = colors.onPrimary,
            checkedTrackColor = colors.primary,
            checkedBorderColor = colors.primary,
            checkedIconColor = colors.onPrimary,
            uncheckedThumbColor = colors.onSurfaceVariant,
            uncheckedTrackColor = colors.background,
            uncheckedBorderColor = colors.outlineVariant,
            uncheckedIconColor = colors.onSurfaceVariant,
            disabledCheckedThumbColor = colors.onPrimary.copy(alpha = disabledAlpha),
            disabledCheckedTrackColor = colors.primary.copy(alpha = disabledAlpha),
            disabledCheckedBorderColor = colors.primary.copy(alpha = disabledAlpha),
            disabledCheckedIconColor = colors.onPrimary.copy(alpha = disabledAlpha),
            disabledUncheckedThumbColor = colors.onSurfaceVariant.copy(alpha = disabledAlpha),
            disabledUncheckedTrackColor = colors.background.copy(alpha = disabledAlpha),
            disabledUncheckedBorderColor = colors.outlineVariant.copy(alpha = disabledAlpha),
            disabledUncheckedIconColor = colors.onSurfaceVariant.copy(alpha = disabledAlpha),
        ),
    )
}
