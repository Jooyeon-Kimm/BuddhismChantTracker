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
fun DayCell(
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

fun buildMonthCells(ym: YearMonth): List<LocalDate> {
    val first = ym.atDay(1)
    val startOffset = (first.dayOfWeek.value % 7) // 일요일=0
    val start = first.minusDays(startOffset.toLong())
    // 6주 * 7칸 = 42칸
    return (0 until 42).map { start.plusDays(it.toLong()) }
}


