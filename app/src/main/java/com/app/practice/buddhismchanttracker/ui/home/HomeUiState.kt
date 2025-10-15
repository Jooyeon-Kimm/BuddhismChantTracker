package com.app.practice.buddhismchanttracker.ui.home

import com.app.practice.buddhismchanttracker.data.model.ChantSession

data class HomeUiState(
    val todayKorean: String = "",
    val type: ChantType = ChantType.GWANSEUM,
    val customText: String = "",
    val running: ChantSession? = null,
    val count: Int = 0,
    val todaySessions: List<ChantSession> = emptyList(),
    val listening: Boolean = false
)