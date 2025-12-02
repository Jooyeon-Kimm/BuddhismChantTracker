package com.app.practice.buddhismchanttracker.ui.settings

import com.app.practice.buddhismchanttracker.ui.home.ChantType

enum class StatsAggregation {
    HOUR, DAY, WEEK, MONTH, YEAR
}

data class TimePoint(
    val label: String,  // "10시", "2025-12-01", "2025-W48", "2025-12" 등
    val total: Int
)

// ui/settings/SettingsUiState.kt
data class SettingsUiState(
    val userName: String? = null,
    val userEmail: String? = null,
    val providerLabel: String? = null,
    val loggedIn: Boolean = false,

    // 통계 관련
    val aggregation: StatsAggregation = StatsAggregation.DAY,
    val allTypesMode: Boolean = true,
    val selectedType: ChantType? = null,
    val availableTypes: List<ChantType> = ChantType.values().toList(),
    val points: List<TimePoint> = emptyList(),

    // 로그인
    val isAuthLoading: Boolean = false,
    val lastAuthError: String? = null,
    val lastAuthMessage: String? = null,   // 성공 메시지용
)
