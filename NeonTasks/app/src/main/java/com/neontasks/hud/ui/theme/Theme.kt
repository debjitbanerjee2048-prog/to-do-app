package com.neontasks.hud.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.neontasks.hud.data.Priority

val HudBlack = Color(0xFF0A0E17)
val HudPanel = Color(0xFF111827)
val NeonCyan = Color(0xFF00F0FF)
val NeonAmber = Color(0xFFFFB300)
val AlertRed = Color(0xFFFF3D5A)
val TextBright = Color(0xFFE6F1FF)
val TextDim = Color(0xFF7A8BA3)

fun Priority.color(): Color = when (this) {
    Priority.CRITICAL -> AlertRed
    Priority.HIGH -> NeonAmber
    Priority.ROUTINE -> NeonCyan
}

val Priority.label: String
    get() = when (this) {
        Priority.CRITICAL -> "Critical"
        Priority.HIGH -> "High"
        Priority.ROUTINE -> "Routine"
    }

private val Mono = FontFamily.Monospace

private val HudTypography = Typography(
    headlineMedium = TextStyle(fontFamily = Mono, fontWeight = FontWeight.Bold, fontSize = 26.sp, letterSpacing = 1.sp),
    titleMedium = TextStyle(fontFamily = Mono, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = Mono, fontSize = 14.sp),
    labelSmall = TextStyle(fontFamily = Mono, fontSize = 11.sp, letterSpacing = 0.8.sp)
)

private val HudColors = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonAmber,
    background = HudBlack,
    surface = HudPanel,
    error = AlertRed,
    onPrimary = HudBlack,
    onBackground = TextBright,
    onSurface = TextBright
)

@Composable
fun HudTheme(content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = HudColors, typography = HudTypography, content = content)
