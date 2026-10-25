package com.neontasks.hud.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.neontasks.hud.data.PriorityEngine
import com.neontasks.hud.ui.theme.*

@Composable
fun TodoScreen(vm: TaskViewModel = viewModel()) {
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    val core by vm.core.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = HudBlack,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                containerColor = NeonCyan, contentColor = HudBlack,
                shape = CutCornerShape(topEnd = 14.dp, bottomStart = 14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("New task", style = MaterialTheme.typography.titleMedium)
            }
        }
    ) { pad ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp, pad.calculateTopPadding() + 12.dp, 16.dp, pad.calculateBottomPadding() + 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Today", color = NeonCyan, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))
                    PowerCore(core.done, core.total)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Swipe left to vaporize · swipe right to defer 3h",
                        color = TextDim, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center
                    )
                }
            }
            if (tasks.isEmpty()) {
                item {
                    Text(
                        "No tasks yet. Tap New task to add one.",
                        color = TextDim, style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp), textAlign = TextAlign.Center
                    )
                }
            }
            items(tasks, key = { it.id }) { task ->
                SwipeableTaskCard(
                    task = task,
                    onComplete = { vm.toggleComplete(task) },
                    onSnooze = { vm.snooze3h(task) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }

    if (showAdd) {
        AddTaskDialog(onDismiss = { showAdd = false }, onAdd = { title, deadline ->
            vm.add(title, deadline); showAdd = false
        })
    }
}

@Composable
private fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (String, Long?) -> Unit) {
    var title by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf<Int?>(null) }
    val deadline = hours?.let { System.currentTimeMillis() + it * 3_600_000L }
    val preview = PriorityEngine.classify(title, "", deadline)
    val options: List<Pair<String, Int?>> = listOf("None" to null, "2h" to 2, "24h" to 24, "3 days" to 72)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HudPanel,
        shape = CutCornerShape(topEnd = 20.dp, bottomStart = 20.dp),
        title = { Text("New task", color = NeonCyan, style = MaterialTheme.typography.headlineMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    placeholder = { Text("What needs doing?", color = TextDim) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan, unfocusedBorderColor = TextDim,
                        cursorColor = NeonCyan, focusedTextColor = TextBright, unfocusedTextColor = TextBright
                    )
                )
                Text("Due in", color = TextDim, style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { (label, h) -> HudChip(label, hours == h) { hours = h } }
                }
                Text(
                    "Auto-priority: ${preview.label}",
                    color = preview.color(), style = MaterialTheme.typography.titleMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (title.isNotBlank()) onAdd(title, deadline) }) {
                Text("Add task", color = NeonCyan)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextDim) } }
    )
}

@Composable
private fun HudChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val c = if (selected) NeonAmber else TextDim
    val shape = CutCornerShape(6.dp)
    Text(
        label, color = c, style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .clip(shape)
            .background(if (selected) c.copy(alpha = 0.15f) else Color.Transparent)
            .border(1.dp, c, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp)
    )
}
