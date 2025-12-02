package com.app.practice.buddhismchanttracker.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.practice.buddhismchanttracker.data.model.chant.ChantSession
import com.app.practice.buddhismchanttracker.data.repository.ChantRepository
import com.app.practice.buddhismchanttracker.ui.home.CountLogEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repo: ChantRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(CalendarUiState())
    val ui: StateFlow<CalendarUiState> = _ui.asStateFlow()

    private val dateFormatter =
        DateTimeFormatter.ofPattern("yyyy년 M월 d일 E요일", Locale.KOREAN)

    init {
        // 초기 선택 날짜 문구 세팅
        _ui.update {
            it.copy(
                selectedDateKorean = it.selectedDate.format(dateFormatter)
            )
        }

        // 1) 선택 날짜 변경 -> 세션 스트림 구독 (쓰고 있으면 유지)
        viewModelScope.launch {
            ui.map { it.selectedDate }
                .distinctUntilChanged()
                .flatMapLatest { date ->
                    // Repo에서 LocalDate 기반으로 세션 가져오는 함수
                    repo.sessionsOfDay(date)
                }
                .collect { list ->
                    _ui.update { s -> s.copy(sessions = list) }
                }
        }

        // 2) 선택 날짜 변경 -> 로그(버튼 + 음성) 스트림 구독
        viewModelScope.launch {
            ui.map { it.selectedDate }
                .distinctUntilChanged()
                .flatMapLatest { date ->
                    // Home 이 사용하는 것과 동일한 함수로 맞춰줘야 함
                    repo.logsOfDay(date)
                }
                .collect { logs ->
                    _ui.update { s -> s.copy(logsOfDay = logs) }
                }
        }

        // 3) 선택 월 변경 -> 해당 월 일자별 합계 스트림 구독
        viewModelScope.launch {
            ui.map { it.selectedMonth }
                .distinctUntilChanged()
                .flatMapLatest { ym ->
                    repo.monthTotals(ym)
                }
                .collect { rows ->
                    val map = rows.associate { row ->
                        LocalDate.parse(row.ymd) to row.total
                    }
                    _ui.update { it.copy(dayTotals = map) }
                }
        }
    }

    fun openPicker(open: Boolean) {
        _ui.update { it.copy(showDatePicker = open) }
    }

    fun pickDate(date: LocalDate) {
        _ui.update {
            it.copy(
                selectedDate = date,
                selectedDateKorean = date.format(dateFormatter),
                showDatePicker = false,
            )
        }
        // 다른 달 클릭했을 때, 선택 월도 같이 맞추기
        _ui.update { it.copy(selectedMonth = YearMonth.from(date)) }
    }

    fun goToday() = pickDate(LocalDate.now())
    fun prevDay() = pickDate(_ui.value.selectedDate.minusDays(1))
    fun nextDay() = pickDate(_ui.value.selectedDate.plusDays(1))
    fun prevMonth() = setMonth(_ui.value.selectedMonth.minusMonths(1))
    fun nextMonth() = setMonth(_ui.value.selectedMonth.plusMonths(1))

    fun setMonth(ym: YearMonth) {
        _ui.update { it.copy(selectedMonth = ym) }
        // 월만 바꿨을 때, 선택 날짜가 다른 달이면 그 달 1일로 스냅
        val day = _ui.value.selectedDate
        if (YearMonth.from(day) != ym) {
            pickDate(ym.atDay(1))
        }
    }

    // 작은 헬퍼: StateFlow update 확장
    private inline fun <T> MutableStateFlow<T>.update(block: (T) -> T) {
        value = block(value)
    }
}
