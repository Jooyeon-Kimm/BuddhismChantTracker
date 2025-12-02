package com.app.practice.buddhismchanttracker.data.model.chant

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chant_logs")
data class ChantLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val ymd: String,           // 2025-12-02 이런 형식 (세션 날짜)
    val timestamp: Long,       // 시작 시각 또는 이벤트 시각 (millis)
    val source: String,        // "VOICE", "MANUAL_SMALL", "MANUAL_BIG"
    val delta: Int,
    val total: Int,
    val endTimestamp: Long?    // VOICE 세션 종료 시각 (없으면 진행 중)
)
