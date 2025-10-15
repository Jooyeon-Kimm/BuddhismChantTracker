package com.app.practice.buddhismchanttracker.data.repository

import com.app.practice.buddhismchanttracker.data.model.ChantDao
import com.app.practice.buddhismchanttracker.data.model.ChantSession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class ChantRepository(private val dao: ChantDao) {
    fun sessionsOfDay(date: LocalDate): Flow<List<ChantSession>> =
        dao.sessionsOfDay(date.toString())

    fun monthTotals(yearMonth: java.time.YearMonth): Flow<List<com.app.practice.buddhismchanttracker.data.model.DayCount>> {
        val from = yearMonth.atDay(1).toString()
        val to = yearMonth.atEndOfMonth().toString()
        return dao.dayTotals(from, to)
    }

    suspend fun startSession(
        typeLabel: String,
        custom: String?,
        now: Long,
        ymd: String,
    ): ChantSession {
        val s = ChantSession(
            typeLabel = typeLabel,
            customLabel = custom,
            startedAt = now,
            endedAt = null,
            count = 0,
            ymd = ymd
        )
        val id = dao.insert(s)
        return s.copy(id = id)
    }

    suspend fun setCount(session: ChantSession, newCount: Int) =
        dao.update(session.copy(count = newCount))

    suspend fun stopSession(session: ChantSession, endMillis: Long) =
        dao.update(session.copy(endedAt = endMillis))

    suspend fun currentRunningOrNull(): ChantSession? = dao.currentRunningOrNull()

    // --- DEV: seed helper ---
    suspend fun insertAll(sessions: List<ChantSession>) {
        dao.insertAll(sessions)
    }
}