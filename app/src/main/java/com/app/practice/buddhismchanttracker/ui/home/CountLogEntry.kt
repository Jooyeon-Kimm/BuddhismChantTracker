package com.app.practice.buddhismchanttracker.ui.home

data class CountLogEntry(
    val timestamp: Long,      // 언제 발생했는지
    val source: CountType,    // VOICE / MANUAL_SMALL / MANUAL_BIG
    val delta: Int,           // +1, -1, +10 이런 변화량
    val total: Int            // 변화 이후 총 count
)