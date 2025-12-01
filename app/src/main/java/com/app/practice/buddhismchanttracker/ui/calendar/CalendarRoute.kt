package com.app.practice.buddhismchanttracker.ui.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

@Composable
fun CalendarRoute(
    vm: CalendarViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()
    val sdf = remember { SimpleDateFormat("a hh시 mm분 ss초", Locale.KOREAN) }

    var showMonthPicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 상단 제목 + 오늘 버튼
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "달력",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = vm::goToday) {
                    Text("오늘")
                }
            }
        }

        // 월 헤더 (클릭 시 연/월 선택 다이얼로그)
        item {
            Text(
                "${ui.selectedMonth.year}년 ${ui.selectedMonth.month.value}월",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { showMonthPicker = true }
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

        // 달력 그리드 (슬라이드로 이전/다음달 전환)
        item {
            val monthDates = remember(ui.selectedMonth) { buildMonthCells(ui.selectedMonth) }
            var dragOffsetX by remember { mutableStateOf(0f) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .pointerInput(ui.selectedMonth) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, delta ->
                                dragOffsetX += delta
                            },
                            onDragEnd = {
                                if (dragOffsetX > 80f) {
                                    // 오른쪽 스와이프 → 이전달
                                    vm.prevMonth()
                                } else if (dragOffsetX < -80f) {
                                    // 왼쪽 스와이프 → 다음달
                                    vm.nextMonth()
                                }
                                dragOffsetX = 0f
                            },
                            onDragCancel = { dragOffsetX = 0f }
                        )
                    }
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    userScrollEnabled = false,
                    modifier = Modifier.fillMaxSize()
                ) {
                    gridItems(monthDates) { day ->
                        val inMonth = YearMonth.from(day) == ui.selectedMonth
                        DayCell(
                            date = day,
                            inMonth = inMonth,
                            selected = day == ui.selectedDate,
                            total = ui.dayTotals[day] ?: 0,
                            onClick = { vm.pickDate(day) }
                        )
                    }
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

    if (showMonthPicker) {
        MonthPickerDialog(
            initialYear = ui.selectedMonth.year,
            initialMonth = ui.selectedMonth.monthValue,
            onConfirm = { year, month ->
                vm.setMonth(YearMonth.of(year, month))
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }
}