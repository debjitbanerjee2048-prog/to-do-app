package com.neontasks.hud.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.neontasks.hud.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class CoreStats(val done: Int = 0, val total: Int = 0)

class TaskViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).taskDao()
    private val threeHours = 3 * 3_600_000L

    val tasks: StateFlow<List<Task>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Today = finished today + open tasks due by end of today (or with no deadline). */
    val core: StateFlow<CoreStats> = tasks.map { list ->
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        val end = start + 24 * 3_600_000L
        val done = list.count { (it.completedAt ?: 0L) >= start }
        val open = list.count { it.completedAt == null && (it.deadline == null || it.deadline < end) }
        CoreStats(done, done + open)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CoreStats())

    fun add(title: String, deadline: Long?) = viewModelScope.launch {
        val t = title.trim()
        dao.insert(Task(title = t, deadline = deadline, priority = PriorityEngine.classify(t, "", deadline)))
    }

    fun toggleComplete(t: Task) = viewModelScope.launch {
        dao.update(t.copy(completedAt = if (t.completedAt == null) System.currentTimeMillis() else null))
    }

    fun snooze3h(t: Task) = viewModelScope.launch {
        val newDeadline = (t.deadline ?: System.currentTimeMillis()) + threeHours
        dao.update(t.copy(deadline = newDeadline, priority = PriorityEngine.classify(t.title, t.notes, newDeadline)))
    }

    fun delete(t: Task) = viewModelScope.launch { dao.delete(t) }
}
