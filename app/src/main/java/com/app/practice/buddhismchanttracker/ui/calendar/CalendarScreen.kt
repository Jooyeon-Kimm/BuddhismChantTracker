package com.app.practice.buddhismchanttracker.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun CalendarRoute(vm: CalendarViewModel = viewModel()) {
    val ui by vm.ui.collectAsState()
    val sdf = remember { SimpleDateFormat("a hh시 mm분 ss초", Locale.KOREAN) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 상단 제목 + 월 이동 + 샘플 버튼
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("달력", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = { vm.seedSamplesForSelectedDate() }) { Text("샘플 데이터 넣기") }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = vm::prevMonth) { Text("◀ 이전달") }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = { vm.setMonth(YearMonth.now()) }) { Text("이번달") }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = vm::nextMonth) { Text("다음달 ▶") }
            }
        }

        // 월 헤더
        item {
            Text(
                "${ui.selectedMonth.year}년 ${ui.selectedMonth.month.value}월",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 요일 헤더 (일~토)
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val days = listOf(
                    DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
                )
                days.forEach {
                    Text(
                        it.getDisplayName(TextStyle.NARROW, Locale.KOREAN),
                        modifier = Modifier.width(40.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // 달력 그리드 (스크롤 비활성화, 고정 높이)
        item {
            val monthDates = remember(ui.selectedMonth) { buildMonthCells(ui.selectedMonth) }
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                userScrollEnabled = false,                // 중첩 스크롤 방지
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                gridItems(monthDates) { day ->
                    DayCell(
                        date = day,
                        inMonth = YearMonth.from(day) == ui.selectedMonth,
                        selected = day == ui.selectedDate,
                        total = ui.dayTotals[day] ?: 0,
                        onClick = { vm.pickDate(day) }
                    )
                }
            }
        }

        // 선택된 날짜 라벨
        item {
            Text(
                ui.selectedDateKorean,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 기록 제목
        item {
            Text("기록", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        // 기록 리스트 (시·분·초 표시)
        if (ui.sessions.isEmpty()) {
            item {
                Text("해당 날짜 기록이 없어요.")
            }
        } else {
            items(ui.sessions) { s ->
                val start = sdf.format(Date(s.startedAt))
                val end = s.endedAt?.let { sdf.format(Date(it)) } ?: "진행 중"
                Text("· $start  -  $end   ${s.count}회")
                Spacer(Modifier.height(4.dp))
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    inMonth: Boolean,
    selected: Boolean,
    total: Int,
    onClick: () -> Unit,
) {
    val border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .padding(2.dp)
            .size(40.dp),
        border = border,
        contentPadding = PaddingValues(0.dp),
        enabled = inMonth
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("${date.dayOfMonth}", style = MaterialTheme.typography.bodyMedium)
            if (total > 0) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                ) {
                    AssistChip(onClick = onClick, label = { Text(total.toString()) })
                }
            }
        }
    }
}

private fun buildMonthCells(ym: YearMonth): List<LocalDate> {
    val first = ym.atDay(1)
    val startOffset = (first.dayOfWeek.value % 7) // 일요일=0
    val start = first.minusDays(startOffset.toLong())
    // 6주 * 7칸 = 42칸
    return (0 until 42).map { start.plusDays(it.toLong()) }
}
