package com.app.practice.buddhismchanttracker.ui.home

import com.app.practice.buddhismchanttracker.data.model.chant.ChantSession

data class HomeUiState(
    val todayDate: String = "",
    val type: ChantType = ChantType.GWANSEUM,
    val customText: String = "",
    val running: ChantSession? = null,
    val count: Int = 0,
    val todaySessions: List<ChantSession> = emptyList(),
    val listening: Boolean = false,

    val inputText: String = "",
    val items: List<ChantItem> = emptyList(),
    val deleteMode: Boolean = false,

    // +++ 버튼용 증가 단위
    val bigStep: Int = 10,
    val heardText: String = "",

    val countLogs: List<CountLogEntry> = emptyList(),

    // 로그 삭제 모드
    val logDeleteMode: Boolean = false,
    val selectedLogTimestamps: Set<Long> = emptySet(),
)
