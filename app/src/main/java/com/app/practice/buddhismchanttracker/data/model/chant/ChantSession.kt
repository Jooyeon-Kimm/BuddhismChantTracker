package com.app.practice.buddhismchanttracker.data.model.chant

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentSnapshot

@Entity(tableName = "chant_sessions")
data class ChantSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val typeLabel: String,           // "관세음보살" 등
    val customLabel: String?,        // 직접 입력 시 텍스트
    val startedAt: Long,             // epoch millis
    val endedAt: Long?,              // 진행 중이면 null
    val count: Int,                  // 세션 누계
    val ymd: String,                  // "2025-10-13"

    val userId: String? = null,      // 이 기록이 어느 Firebase 계정의 것인지
    val remoteId: String? = null     // Firestore document id 같은 것 (옵션)
)

fun ChantSession.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "typeLabel" to typeLabel,
    "customLabel" to customLabel,
    "startedAt" to startedAt,
    "endedAt" to endedAt,
    "count" to count,
    "ymd" to ymd,
    "userId" to userId,
)

fun DocumentSnapshot.toChantSession(): ChantSession? {
    return try {
        ChantSession(
            id = (getLong("id") ?: 0L),
            typeLabel = getString("typeLabel") ?: "",
            customLabel = getString("customLabel"),
            startedAt = getLong("startedAt") ?: 0L,
            endedAt = getLong("endedAt"),
            count = (getLong("count") ?: 0L).toInt(),
            ymd = getString("ymd") ?: "",
            userId = getString("userId")
        )
    } catch (e: Exception) {
        null
    }
}
