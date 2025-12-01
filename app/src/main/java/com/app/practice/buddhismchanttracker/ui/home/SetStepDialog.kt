package com.app.practice.buddhismchanttracker.ui.home

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun SetStepDialog(
    initial: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initial.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("증가 단위 설정") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { s -> text = s.filter { it.isDigit() } },
                label = { Text("한 번에 증가할 회수 (1 이상)") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val v = text.toIntOrNull()
                if (v != null && v > 0) {
                    onConfirm(v)
                }
            }) { Text("확인") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}
