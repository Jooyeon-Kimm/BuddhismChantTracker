package com.app.practice.buddhismchanttracker.ui.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.practice.buddhismchanttracker.data.model.ChantDb
import com.app.practice.buddhismchanttracker.data.model.ChantSession
import com.app.practice.buddhismchanttracker.data.repository.ChantRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId.systemDefault
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ChantRepository(ChantDb.get(app).dao())

    private val _ui = MutableStateFlow(CalendarUiState())
    val ui: StateFlow<CalendarUiState> = _ui.asStateFlow()

    private val dateFormatter =
        DateTimeFormatter.ofPattern("yyyy년 M월 d일 E요일", Locale.KOREAN)

    init {
        // 초기 날짜 문구 설정
        _ui.update {
            it.copy(
                selectedDateKorean = it.selectedDate.format(dateFormatter)
            )
        }
        // 선택된 날짜가 바뀔 때마다 DB 스트림 다시 구독
        viewModelScope.launch {
            ui.map { it.selectedDate }
                .distinctUntilChanged()
                .flatMapLatest { date -> repo.sessionsOfDay(date) }
                .collect { list ->
                    _ui.update { s -> s.copy(sessions = list) }
                }
        }

        viewModelScope.launch {
            ui.map { it.selectedMonth }
                .distinctUntilChanged()
                .flatMapLatest { ym -> repo.monthTotals(ym) }
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
                showDatePicker = false
            )
        }

        // 달 넘어 클릭 시 월 동기화
        _ui.update { it.copy(selectedMonth = YearMonth.from(date)) }
    }

    fun goToday() = pickDate(LocalDate.now())
    fun prevDay() = pickDate(_ui.value.selectedDate.minusDays(1))
    fun nextDay() = pickDate(_ui.value.selectedDate.plusDays(1))
    fun prevMonth() = setMonth(_ui.value.selectedMonth.minusMonths(1))
    fun nextMonth() = setMonth(_ui.value.selectedMonth.plusMonths(1))
    fun setMonth(ym: YearMonth) {
        _ui.update { it.copy(selectedMonth = ym) }
        // 월만 바뀐 경우, 선택 날짜를 해당 월 안으로 스냅
        val day = _ui.value.selectedDate
        if (YearMonth.from(day) != ym) pickDate(ym.atDay(1))
    }


    /** TEST **/
    // --- DEV: 선택된 날짜에 더미 데이터 넣기 ---
    fun seedSamplesForSelectedDate() {
        val date = _ui.value.selectedDate
        val ymd = date.toString()

        // 오전 9:10~9:20, 오후 1:34~1:45, 밤 9:00~9:12 같이 3개 세션 예시
        fun millisAt(h: Int, m: Int, s: Int) =
            java.time.ZonedDateTime.of(
                date,
                LocalTime.of(h, m, s),
                systemDefault()
            )
                .toInstant().toEpochMilli()

        val samples = listOf(
            ChantSession(
                typeLabel = "관세음보살",
                customLabel = null,
                startedAt = millisAt(9, 10, 1),
                endedAt = millisAt(9, 20, 48),
                count = 33,
                ymd = ymd
            ),
            ChantSession(
                typeLabel = "나무 아미타불",
                customLabel = null,
                startedAt = millisAt(13, 34, 42),
                endedAt = millisAt(13, 50, 5),
                count = 29,
                ymd = ymd
            ),
            ChantSession(
                typeLabel = "지장보살",
                customLabel = null,
                startedAt = millisAt(21, 0, 0),
                endedAt = millisAt(21, 12, 30),
                count = 18,
                ymd = ymd
            )
        )
        viewModelScope.launch { repo.insertAll(samples) }
    }
}
