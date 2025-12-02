package com.app.practice.buddhismchanttracker.data.repository

import com.app.practice.buddhismchanttracker.data.model.chant.ChantDao
import com.app.practice.buddhismchanttracker.data.model.chant.ChantSession
import com.app.practice.buddhismchanttracker.data.model.chant.DayCount
import com.app.practice.buddhismchanttracker.data.model.chant.ChantLogEntity
import com.app.practice.buddhismchanttracker.data.model.chant.toChantSession
import com.app.practice.buddhismchanttracker.data.model.chant.toFirestoreMap
import com.app.practice.buddhismchanttracker.ui.home.ChantType
import com.app.practice.buddhismchanttracker.ui.home.CountLogEntry
import com.app.practice.buddhismchanttracker.ui.home.CountType
import com.app.practice.buddhismchanttracker.ui.settings.StatsAggregation
import com.app.practice.buddhismchanttracker.ui.settings.TimePoint
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChantRepositoryImpl @Inject constructor(
    private val dao: ChantDao,
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore,

    ) : ChantRepository {

    override fun sessionsOfDay(date: LocalDate): Flow<List<ChantSession>> =
        dao.sessionsOfDay(date.toString())

    override fun monthTotals(yearMonth: YearMonth): Flow<List<DayCount>> {
        val from = yearMonth.atDay(1).toString()
        val to = yearMonth.atEndOfMonth().toString()
        return dao.dayTotals(from, to)
    }

    override suspend fun startSession(
        typeLabel: String,
        custom: String?,
        now: Long,
        ymd: String,
    ): ChantSession {
        val currentUser = authRepository.currentUser.value
        val base = ChantSession(
            typeLabel = typeLabel,
            customLabel = custom,
            startedAt = now,
            endedAt = null,
            count = 0,
            ymd = ymd,
            userId = currentUser?.id  // 로그인된 유저 id
        )
        // 실제 저장은 addChantSession 에게 맡김
        return addChantSession(base)
    }


    override suspend fun setCount(session: ChantSession, newCount: Int) {
        val updated = session.copy(count = newCount)
        dao.update(updated)

        val userId = updated.userId ?: authRepository.currentUser.value?.id
        if (userId != null && updated.id != 0L) {
            firestore.collection("users")
                .document(userId)
                .collection("chantSessions")
                .document(updated.id.toString())
                .update("count", newCount)
        }
    }

    override suspend fun stopSession(session: ChantSession, endMillis: Long) {
        val updated = session.copy(endedAt = endMillis)
        dao.update(updated)

        val userId = updated.userId ?: authRepository.currentUser.value?.id
        if (userId != null && updated.id != 0L) {
            firestore.collection("users")
                .document(userId)
                .collection("chantSessions")
                .document(updated.id.toString())
                .update("endedAt", endMillis)
        }
    }


    override suspend fun currentRunningOrNull(): ChantSession? =
        dao.currentRunningOrNull()

    // ===== 통계 =====
    override suspend fun loadStats(
        aggregation: StatsAggregation,
        chantType: ChantType?
    ): List<TimePoint> {
        val allSessions = dao.getAllSessions()
        if (allSessions.isEmpty()) return emptyList()

        val filtered = chantType?.let { type ->
            allSessions.filter { session ->
                when (type) {
                    ChantType.CUSTOM ->
                        session.customLabel?.isNotBlank() == true
                    else ->
                        session.typeLabel == type.label
                }
            }
        } ?: allSessions

        if (filtered.isEmpty()) return emptyList()

        val grouped: Map<String, Int> = when (aggregation) {
            StatsAggregation.HOUR -> {
                filtered
                    .groupBy { session ->
                        val dateTime = java.time.Instant.ofEpochMilli(session.startedAt)
                            .atZone(java.time.ZoneId.systemDefault())
                        val hour = dateTime.hour
                        String.format("%02d시", hour)
                    }
                    .mapValues { (_, list) -> list.sumOf { it.count } }
            }

            StatsAggregation.DAY -> {
                filtered
                    .groupBy { it.ymd }
                    .mapValues { (_, list) -> list.sumOf { it.count } }
            }

            StatsAggregation.WEEK -> {
                val weekFields = WeekFields.of(Locale.getDefault())
                filtered
                    .groupBy { session ->
                        val date = LocalDate.parse(session.ymd)
                        val year = date.year
                        val week = date.get(weekFields.weekOfWeekBasedYear())
                        String.format("%04d-%02d주", year, week)
                    }
                    .mapValues { (_, list) -> list.sumOf { it.count } }
            }

            StatsAggregation.MONTH -> {
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
                filtered
                    .groupBy { session ->
                        val date = LocalDate.parse(session.ymd)
                        "${date.year}년"
                    }
                    .mapValues { (_, list) -> list.sumOf { it.count } }
            }
        }

        return grouped
            .toList()
            .sortedBy { (label, _) -> label }
            .map { (label, total) ->
                TimePoint(label = label, total = total)
            }
    }

    // ===== 로그 =====
    override fun logsOfDay(date: LocalDate): Flow<List<CountLogEntry>> {
        val ymd = date.toString()
        return dao.logsOfDay(ymd).map { list ->
            list.map { e ->
                CountLogEntry(
                    timestamp = e.timestamp,
                    source = CountType.valueOf(e.source),
                    delta = e.delta,
                    total = e.total,
                    endTimestamp = e.endTimestamp
                )
            }
        }
    }

    override suspend fun deleteLogsByTimestamps(timestamps: Set<Long>) {
        if (timestamps.isEmpty()) return
        dao.deleteLogsByTimestamps(timestamps.toList())
    }

    override suspend fun addChantSession(session: ChantSession): ChantSession {
        // 1) Room 에 저장
        val localId = dao.insert(session)
        val saved = session.copy(id = localId)

        // 2) 로그인 상태라면 Firestore 에도 업로드
        val user = authRepository.currentUser.value
        val userId = user?.id

        if (userId != null) {
            val docRef = firestore
                .collection("users")
                .document(userId)
                .collection("chantSessions")
                // localId 를 그대로 문서 ID 로 쓰면 나중에 업데이트 편함
                .document(localId.toString())

            docRef.set(saved.toFirestoreMap())
        }

        return saved
    }


    override suspend fun syncFromCloudForCurrentUser() {
        val user = authRepository.currentUser.value ?: return

        val snapshot = firestore
            .collection("users")
            .document(user.id)
            .collection("chantSessions")
            .get()
            .await()   // kotlinx-coroutines-play-services 필요

        val sessions = snapshot.documents.mapNotNull { doc ->
            doc.toChantSession()
        }

        // 여기선 단순하게 "이 유저 데이터 싹 갈아끼우기" 예제
        // 필요하면 userId 기준으로 delete + insertAll 등으로 정책 조정 가능
        dao.insertAll(sessions)
    }


    override suspend fun insertLog(entry: CountLogEntry, ymd: String) {
        val entity = ChantLogEntity(
            ymd = ymd,
            timestamp = entry.timestamp,
            source = entry.source.name,
            delta = entry.delta,
            total = entry.total,
            endTimestamp = entry.endTimestamp
        )
        dao.insertLog(entity)
    }
}
