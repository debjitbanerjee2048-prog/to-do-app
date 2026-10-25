package com.neontasks.hud.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.neontasks.hud.data.Task
import com.neontasks.hud.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val cardShape = CutCornerShape(topEnd = 16.dp, bottomStart = 16.dp)

/** Swipe left = complete ("vaporize"), swipe right = defer 3 hours. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTaskCard(
    task: Task,
    onComplete: () -> Unit,
    onSnooze: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val complete by rememberUpdatedState(onComplete)
    val snooze by rememberUpdatedState(onSnooze)

    val state = rememberSwipeToDismissBoxState(confirmValueChange = { value ->
        when (value) {
            SwipeToDismissBoxValue.EndToStart -> {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress); complete()
            }
            SwipeToDismissBoxValue.StartToEnd -> {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); snooze()
            }
            else -> Unit
        }
        false // always spring back; the list updates from the database
    })

    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        backgroundContent = {
            val dir = state.dismissDirection
            val (text, color, align) = when (dir) {
                SwipeToDismissBoxValue.EndToStart -> Triple(if (task.completedAt == null) "Vaporize" else "Restore", NeonCyan, Alignment.CenterEnd)
                SwipeToDismissBoxValue.StartToEnd -> Triple("Defer 3h", NeonAmber, Alignment.CenterStart)
                else -> Triple("", Color.Transparent, Alignment.Center)
            }
            Box(
                Modifier.fillMaxSize().clip(cardShape).background(color.copy(alpha = 0.12f)).padding(horizontal = 22.dp),
                contentAlignment = align
            ) { Text(text, color = color, style = MaterialTheme.typography.titleMedium) }
        }
    ) { TaskCard(task) }
}

@Composable
fun TaskCard(task: Task) {
    val done = task.completedAt != null
    val accent = task.priority.color()
    val fx = rememberInfiniteTransition(label = "fx")
    val glow by fx.animateFloat(
        initialValue = 0.35f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1700, easing = LinearEasing), RepeatMode.Reverse), label = "glow"
    )
    val scan by fx.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3800, easing = LinearEasing)), label = "scan"
    )
    val fade by animateFloatAsState(if (done) 0.45f else 1f, label = "fade")
    val overdue = !done && task.deadline != null && task.deadline < System.currentTimeMillis()

    Row(
        Modifier
            .fillMaxWidth()
            .alpha(fade)
            .clip(cardShape)
            .background(Brush.verticalGradient(listOf(Color.White.copy(0.08f), Color.White.copy(0.02f))))
            .border(1.dp, accent.copy(alpha = if (done) 0.3f else glow), cardShape)
            .drawWithContent {
                drawContent()
                if (!done) {
                    val band = 36f
                    val y = size.height * scan
                    drawRect(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, accent.copy(0.16f), Color.Transparent),
                            startY = y - band, endY = y + band
                        ),
                        topLeft = Offset(0f, y - band), size = Size(size.width, band * 2)
                    )
                }
                var ly = 0f
                while (ly < size.height) {
                    drawLine(Color.White.copy(0.025f), Offset(0f, ly), Offset(size.width, ly)); ly += 5f
                }
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(3.dp).height(38.dp).background(accent))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                task.title, color = TextBright, style = MaterialTheme.typography.titleMedium,
                textDecoration = if (done) TextDecoration.LineThrough else null
            )
            Spacer(Modifier.height(4.dp))
            Text(
                task.deadline?.let {
                    (if (overdue) "Overdue · " else "Due ") + SimpleDateFormat("EEE HH:mm", Locale.getDefault()).format(Date(it))
                } ?: "No deadline",
                color = if (overdue) AlertRed else TextDim, style = MaterialTheme.typography.labelSmall
            )
        }
        Text(
            task.priority.label,
            color = accent, style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .border(1.dp, accent.copy(0.7f), CutCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
