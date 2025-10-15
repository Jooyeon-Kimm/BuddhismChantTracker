package com.app.practice.buddhismchanttracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chant_sessions")
data class ChantSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val typeLabel: String,           // "관세음보살" 등
    val customLabel: String?,        // 직접 입력 시 텍스트
    val startedAt: Long,             // epoch millis
    val endedAt: Long?,              // 진행 중이면 null
    val count: Int,                  // 세션 누계
    val ymd: String                  // "2025-10-13"
)