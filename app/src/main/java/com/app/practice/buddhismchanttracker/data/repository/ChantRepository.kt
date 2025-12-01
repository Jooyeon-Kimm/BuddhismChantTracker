package com.app.practice.buddhismchanttracker.data.repository

import com.app.practice.buddhismchanttracker.data.model.chant.ChantDao
import com.app.practice.buddhismchanttracker.data.model.chant.ChantSession
import com.app.practice.buddhismchanttracker.data.model.chant.DayCount
import com.app.practice.buddhismchanttracker.ui.home.ChantType
import com.app.practice.buddhismchanttracker.ui.settings.StatsAggregation
import com.app.practice.buddhismchanttracker.ui.settings.TimePoint
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

class ChantRepository @Inject constructor(
    private val dao: ChantDao
) {
    fun sessionsOfDay(date: LocalDate): Flow<List<ChantSession>> =
        dao.sessionsOfDay(date.toString())

    fun monthTotals(yearMonth: java.time.YearMonth): Flow<List<DayCount>> {
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

    /**
     * 통계 데이터 로딩:
     * - aggregation: 시간/일/주/월/년 단위 중 하나
     * - chantType: null 이면 전체 타입 합산, 아니면 해당 타입만 필터링
     */
    suspend fun loadStats(
        aggregation: StatsAggregation,
        chantType: ChantType?
    ): List<TimePoint> {
        // 1) 전체 세션 가져오기
        val allSessions = dao.getAllSessions()
        if (allSessions.isEmpty()) return emptyList()

        // 2) ChantType 필터링 (null이면 전체)
        val filtered = chantType?.let { type ->
            allSessions.filter { session ->
                when (type) {
                    ChantType.CUSTOM ->
                        // "직접 입력"으로 저장된 것들 (customLabel 이 있는 경우)
                        session.customLabel?.isNotBlank() == true
                    else ->
                        // typeLabel 에 "관세음보살" 같은 라벨이 들어가 있으니까 그걸로 비교
                        session.typeLabel == type.label
                }
            }
        } ?: allSessions

        if (filtered.isEmpty()) return emptyList()

        // 3) aggregation 단위에 따라 그룹핑
        val grouped: Map<String, Int> = when (aggregation) {
            StatsAggregation.HOUR -> {
                // 0~23시 시간대별 분포 (모든 날짜 합산)
                filtered
                    .groupBy { session ->
                        // startedAt 기준으로 시간 추출
                        val dateTime = java.time.Instant.ofEpochMilli(session.startedAt)
                            .atZone(java.time.ZoneId.systemDefault())
                        val hour = dateTime.hour
                        String.format("%02d시", hour)
                    }
                    .mapValues { (_, list) -> list.sumOf { it.count } }
            }

            StatsAggregation.DAY -> {
                // ymd("2025-10-13") 문자열 기준으로 일별 집계
                filtered
                    .groupBy { it.ymd }
                    .mapValues { (_, list) -> list.sumOf { it.count } }
            }

            StatsAggregation.WEEK -> {
                // 주 단위: "2025-48주" 이런 식으로 표기
                val weekFields = WeekFields.of(Locale.getDefault())
                filtered
                    .groupBy { session ->
                        val date = LocalDate.parse(session.ymd)  // "YYYY-MM-DD" 형식
                        val year = date.year
                        val week = date.get(weekFields.weekOfWeekBasedYear())
                        String.format("%04d-%02d주", year, week)
                    }
                    .mapValues { (_, list) -> list.sumOf { it.count } }
            }

            StatsAggregation.MONTH -> {
                // 월 단위: "2025-10월"
                filtered
                    .groupBy { session ->
                        val date = LocalDate.parse(session.ymd)
                        val year = date.year
                        val month = date.monthValue
                        String.format("%04d-%02d월", year, month)
                    }
                    .mapValues { (_, list) -> list.sumOf { it.count } }
            }

            StatsAggregation.YEAR -> {
                // 연 단위: "2025년"
                filtered
                    .groupBy { session ->
                        val date = LocalDate.parse(session.ymd)
                        "${date.year}년"
                    }
                    .mapValues { (_, list) -> list.sumOf { it.count } }
            }
        }

        // 4) 라벨 기준으로 정렬 + TimePoint 리스트로 변환
        return grouped
            .toList()
            .sortedBy { (label, _) -> label }
            .map { (label, total) ->
                TimePoint(label = label, total = total)
            }
    }

}