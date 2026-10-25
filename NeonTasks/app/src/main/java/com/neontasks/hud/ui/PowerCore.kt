package com.neontasks.hud.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neontasks.hud.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun PowerCore(done: Int, total: Int, modifier: Modifier = Modifier) {
    val target = if (total == 0) 0f else done / total.toFloat()
    val frac by animateFloatAsState(target, tween(900, easing = FastOutSlowInEasing), label = "frac")
    val spin by rememberInfiniteTransition(label = "spin").animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(14_000, easing = LinearEasing)), label = "spin"
    )

    Box(modifier.size(210.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val c = center
            val stroke = 12.dp.toPx()
            val pad = 26.dp.toPx()
            val arcTL = Offset(pad, pad)
            val arcSize = Size(size.width - 2 * pad, size.height - 2 * pad)

            // slow rotating dashed outer ring
            rotate(spin) {
                drawCircle(
                    NeonCyan.copy(alpha = 0.35f), radius = size.minDimension / 2 - 3.dp.toPx(),
                    style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 16f)))
                )
            }
            // tick marks around the gauge
            val r1 = size.minDimension / 2 - 12.dp.toPx()
            val r2 = r1 - 6.dp.toPx()
            for (i in 0..20) {
                val a = (135 + 270 * i / 20f) * PI.toFloat() / 180f
                drawLine(
                    if (i / 20f <= frac) NeonAmber else TextDim.copy(alpha = 0.5f),
                    Offset(c.x + r1 * cos(a), c.y + r1 * sin(a)),
                    Offset(c.x + r2 * cos(a), c.y + r2 * sin(a)),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
            // track
            drawArc(Color.White.copy(alpha = 0.06f), 135f, 270f, false, arcTL, arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round))
            // glow layers
            for (w in listOf(2.6f, 1.9f)) {
                drawArc(NeonCyan.copy(alpha = 0.07f), 135f, 270f * frac, false, arcTL, arcSize,
                    style = Stroke(stroke * w, cap = StrokeCap.Round))
            }
            // energy arc: amber (low charge) -> cyan (full)
            rotate(135f) {
                drawArc(
                    Brush.sweepGradient(0f to NeonAmber, 0.75f to NeonCyan, 1f to NeonCyan),
                    0f, 270f * frac, false, arcTL, arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${(frac * 100).roundToInt()}%",
                color = NeonCyan, fontSize = 40.sp, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium
            )
            Text("Power core", color = TextDim, style = MaterialTheme.typography.labelSmall)
            Text("$done of $total done today", color = TextBright.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall)
        }
    }
}
