package com.app.practice.buddhismchanttracker.data.model.chant

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.app.practice.buddhismchanttracker.data.model.chant.DayCount
import kotlinx.coroutines.flow.Flow

@Dao
interface ChantDao {
    @Insert
    suspend fun insert(session: ChantSession): Long

    @Insert
    suspend fun insertAll(list: List<ChantSession>): List<Long>

    @Update
    suspend fun update(session: ChantSession)

    @Query("SELECT * FROM chant_sessions WHERE ymd = :ymd ORDER BY startedAt DESC")
    fun sessionsOfDay(ymd: String): Flow<List<ChantSession>>

    @Query("SELECT * FROM chant_sessions WHERE endedAt IS NULL LIMIT 1")
    suspend fun currentRunningOrNull(): ChantSession?

    // -- 월 단위(또는 임의 기간) 집계: 일자별 total
    @Query(
        """
        SELECT ymd AS ymd, SUM(count) AS total
        FROM chant_sessions
        WHERE ymd BETWEEN :fromYmd AND :toYmd
        GROUP BY ymd
    """
    )
    fun dayTotals(fromYmd: String, toYmd: String): Flow<List<DayCount>>

    @Query("DELETE FROM chant_sessions WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("SELECT * FROM chant_sessions")
    suspend fun getAllSessions(): List<ChantSession>
}