package com.neontasks.hud.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Priority { CRITICAL, HIGH, ROUTINE }

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val priority: Priority,
    val deadline: Long? = null,          // epoch millis
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null        // null = still open
)
