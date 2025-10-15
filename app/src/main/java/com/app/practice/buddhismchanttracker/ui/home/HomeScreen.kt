package com.app.practice.buddhismchanttracker.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeRoute(vm: HomeViewModel = viewModel()) {
    val ui by vm.ui.collectAsState()

    HomeScreen(
        dateText = ui.todayKorean,
        type = ui.type,
        onPickType = vm::pickType,
        customText = ui.customText,
        onCustomChange = vm::setCustom,
        running = ui.running != null,
        listening = ui.listening,
        count = ui.count,
        onMinus = vm::dec,
        onPlus1 = { vm.inc(1) },
        onPlus10 = { vm.inc(10) },
        onStartStop = vm::toggleStartStop,
        logs = ui.todaySessions.map { s ->
            val sdf = SimpleDateFormat("a hh시 mm분 ss초", Locale.KOREAN)
            val start = sdf.format(Date(s.startedAt))
            val end = s.endedAt?.let { sdf.format(Date(it)) } ?: "진행 중"
            "· ${start}  -  ${end}   ${s.count}회"
        }
    )
}

@Composable
fun HomeScreen(
    dateText: String,
    type: ChantType,
    onPickType: (ChantType) -> Unit,
    customText: String,
    onCustomChange: (String) -> Unit,
    running: Boolean,
    listening: Boolean,
    count: Int,
    onMinus: () -> Unit,
    onPlus1: () -> Unit,
    onPlus10: () -> Unit,
    onStartStop: () -> Unit,
    logs: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))
        Text(dateText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(20.dp))
        Text("기도 유형", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        Column(Modifier.fillMaxWidth()) {
            ChantTypeRow("나무 아미타불", type == ChantType.NAMU_AMITABUL) { onPickType(ChantType.NAMU_AMITABUL) }
            ChantTypeRow("나무 관세음보살", type == ChantType.NAMU_GWANSEUM) { onPickType(ChantType.NAMU_GWANSEUM) }
            ChantTypeRow("관세음보살", type == ChantType.GWANSEUM) { onPickType(ChantType.GWANSEUM) }
            ChantTypeRow("지장보살", type == ChantType.JIJANG) { onPickType(ChantType.JIJANG) }
            ChantTypeRow("직접 입력", type == ChantType.CUSTOM) { onPickType(ChantType.CUSTOM) }

            if (type == ChantType.CUSTOM) {
                OutlinedTextField(
                    value = customText,
                    onValueChange = onCustomChange,
                    placeholder = { Text("예) 나무 대자대비 관세음보살") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 36.dp, top = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onStartStop) {
                Text(if (running) "종료" else "염불 시작")
            }
            Spacer(Modifier.width(12.dp))
            if (running) {
                AssistChip(
                    onClick = {},
                    label = { Text(if (listening) "음성 인식 중" else "대기") }
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onMinus) { Text("–") }
            Spacer(Modifier.width(24.dp))
            Text("$count 회", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(24.dp))
            OutlinedButton(onClick = onPlus1) { Text("+") }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = onPlus10) { Text("+++") } // +10
        }

        Spacer(Modifier.height(24.dp))
        Text("기록", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        if (logs.isEmpty()) {
            Text("오늘 기록이 아직 없어요.")
        } else {
            logs.forEach { line -> Text(line) }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ChantTypeRow(label: String, checked: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Checkbox(checked = checked, onCheckedChange = { onClick() })
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}