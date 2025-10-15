package com.app.practice.buddhismchanttracker.ui.calendar

import com.app.practice.buddhismchanttracker.data.model.ChantSession
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedDateKorean: String = "",
    val sessions: List<ChantSession> = emptyList(),
    val showDatePicker: Boolean = false,
    val selectedMonth: YearMonth = YearMonth.now(),
    // ymd(LocalDate) -> total
    val dayTotals: Map<LocalDate, Int> = emptyMap()
)
