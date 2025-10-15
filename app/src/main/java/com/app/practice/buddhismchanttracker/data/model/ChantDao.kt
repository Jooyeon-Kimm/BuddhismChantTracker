package com.app.practice.buddhismchanttracker.data.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChantDao {
    @Insert suspend fun insert(session: ChantSession): Long
    @Update
    suspend fun update(session: ChantSession)
    @Query("SELECT * FROM chant_sessions WHERE ymd = :ymd ORDER BY startedAt DESC")
    fun sessionsOfDay(ymd: String): Flow<List<ChantSession>>
    @Query("SELECT * FROM chant_sessions WHERE endedAt IS NULL LIMIT 1")
    suspend fun currentRunningOrNull(): ChantSession?
}