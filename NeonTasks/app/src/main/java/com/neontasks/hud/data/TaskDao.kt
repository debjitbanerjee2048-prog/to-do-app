package com.neontasks.hud.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query(
        """SELECT * FROM tasks ORDER BY
           completedAt IS NOT NULL,
           CASE priority WHEN 'CRITICAL' THEN 0 WHEN 'HIGH' THEN 1 ELSE 2 END,
           deadline IS NULL, deadline"""
    )
    fun observeAll(): Flow<List<Task>>

    @Insert suspend fun insert(task: Task): Long
    @Update suspend fun update(task: Task)
    @Delete suspend fun delete(task: Task)
}
