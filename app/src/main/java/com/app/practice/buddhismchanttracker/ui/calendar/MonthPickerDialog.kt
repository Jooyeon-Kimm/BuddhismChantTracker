package com.app.practice.buddhismchanttracker.ui.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MonthPickerDialog(
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