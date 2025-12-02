package com.app.practice.buddhismchanttracker.data.repository

import com.app.practice.buddhismchanttracker.data.model.chant.ChantSession
import com.app.practice.buddhismchanttracker.data.model.chant.DayCount
import com.app.practice.buddhismchanttracker.ui.home.ChantType
import com.app.practice.buddhismchanttracker.ui.home.CountLogEntry
import com.app.practice.buddhismchanttracker.ui.settings.StatsAggregation
import com.app.practice.buddhismchanttracker.ui.settings.TimePoint
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

interface ChantRepository {

    fun sessionsOfDay(date: LocalDate): Flow<List<ChantSession>>

    fun monthTotals(yearMonth: YearMonth): Flow<List<DayCount>>

    suspend fun startSession(
        typeLabel: String,
        custom: String?,
        now: Long,
        ymd: String,
    ): ChantSession

    suspend fun setCount(session: ChantSession, newCount: Int)

    suspend fun stopSession(session: ChantSession, endMillis: Long)

    suspend fun currentRunningOrNull(): ChantSession?

    suspend fun loadStats(
        aggregation: StatsAggregation,
        chantType: ChantType?
    ): List<TimePoint>

    // ==== 로그 관련 ====
    suspend fun insertLog(entry: CountLogEntry, ymd: String)

    // 오늘 로그 불러오기
    fun logsOfDay(date: LocalDate): Flow<List<CountLogEntry>>

    suspend fun deleteLogsByTimestamps(timestamps: Set<Long>)

    suspend fun addChantSession(session: ChantSession) : ChantSession

    // Firebase로 로그인한 유저 기준으로 Cloud 기록을 Room으로 가져오기
    suspend fun syncFromCloudForCurrentUser()
}
