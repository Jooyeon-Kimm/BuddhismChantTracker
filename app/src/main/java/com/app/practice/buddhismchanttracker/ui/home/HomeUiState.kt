package com.app.practice.buddhismchanttracker.ui.home

import com.app.practice.buddhismchanttracker.data.model.ChantSession

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
)
