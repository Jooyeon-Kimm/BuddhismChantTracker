package com.app.practice.buddhismchanttracker.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import java.util.Date

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

@Composable
private fun DayCell(
    date: LocalDate,
    inMonth: Boolean,
    selected: Boolean,
    total: Int,
    onClick: () -> Unit,
) {
    val border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    val textColor =
        if (inMonth) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)

    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .padding(2.dp)
            .size(40.dp),
        border = border,
        contentPadding = PaddingValues(0.dp),
        enabled = true
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "${date.dayOfMonth}",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
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

// 단순 예제용 (지금 구조에서는 사용 안 해도 됨)
@Composable
fun CalendarContent(
    monthLabel: String,
    days: List<Int>,
    selectedDay: Int? = null,
) {
    Column(Modifier.padding(16.dp)) {
        Text(monthLabel, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        days.chunked(7).forEach { week ->
            Row {
                week.forEach { d ->
                    val isSel = selectedDay == d
                    Box(
                        Modifier
                            .size(36.dp)
                            .padding(2.dp)
                    ) {
                        Text(if (isSel) "[$d]" else "$d")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview_CalendarContent() {
    Surface {
        CalendarContent(
            monthLabel = "2025-10",
            days = (1..31).toList(),
            selectedDay = 16
        )
    }
}

@Composable
private fun MonthPickerDialog(
    initialYear: Int,
    initialMonth: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var yearText by remember { mutableStateOf(initialYear.toString()) }
    var monthText by remember { mutableStateOf(initialMonth.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("연 / 월 선택") },
        text = {
            Column {
                androidx.compose.material3.OutlinedTextField(
                    value = yearText,
                    onValueChange = { yearText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("년 (예: 2025)") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                androidx.compose.material3.OutlinedTextField(
                    value = monthText,
                    onValueChange = { monthText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("월 (1~12)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val y = yearText.toIntOrNull()
                val m = monthText.toIntOrNull()
                if (y != null && m != null && m in 1..12) {
                    onConfirm(y, m)
                }
            }) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}
