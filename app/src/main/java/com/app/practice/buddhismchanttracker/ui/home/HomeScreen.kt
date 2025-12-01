package com.app.practice.buddhismchanttracker.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

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
    onMinus1: () -> Unit,
    onPlus1: () -> Unit,
    bigStep: Int,
    onMinusBig: () -> Unit,
    onPlusBig: () -> Unit,
    onChangeBigStep: (Int) -> Unit,
    onStartStop: () -> Unit,
    logs: List<CountLogEntry>,
    inputText: String,
    onInputChange: (String) -> Unit,
    onConfirmAdd: () -> Unit,
    items: List<ChantItem>,
    deleteMode: Boolean,
    onToggleDeleteMode: () -> Unit,
    onToggleChecked: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    heardText: String,
) {
    // 염불 세션이 진행 중일 때는 유형 변경/추가/삭제 막기
    val canChangeType = !running

    // 기본 기도 유형 라벨 -> ChantType 매핑
    val baseLabelToType = remember {
        linkedMapOf(
            "나무 아미타불" to ChantType.NAMU_AMITABUL,
            "나무 관세음보살" to ChantType.NAMU_GWANSEUM,
            "관세음보살" to ChantType.GWANSEUM,
            "지장보살" to ChantType.JIJANG
        )
    }

    // 화면에 보이는 전체 라벨 리스트 (기본 + 내가 추가한 커스텀)
    val chantLabels = remember {
        mutableStateListOf<String>().apply { addAll(baseLabelToType.keys) }
    }

    // "직접입력으로 추가해서 선택 중인" 커스텀 라벨
    var lastCustomLabel by remember { mutableStateOf<String?>(null) }

    // 현재 선택된 라벨(리스트 항목 중 하나인 경우만)
    val currentLabel: String? = when (type) {
        ChantType.NAMU_AMITABUL -> "나무 아미타불"
        ChantType.NAMU_GWANSEUM -> "나무 관세음보살"
        ChantType.GWANSEUM -> "관세음보살"
        ChantType.JIJANG -> "지장보살"
        ChantType.CUSTOM -> {
            when {
                lastCustomLabel != null && lastCustomLabel in chantLabels -> lastCustomLabel
                customText.isNotBlank() && customText in chantLabels -> customText
                else -> null
            }
        }
    }

    // "직접 입력" 체크 표시 여부
    val directChecked = (type == ChantType.CUSTOM && lastCustomLabel == null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            dateText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))
        Text(
            "기도 유형", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))

        Column(Modifier.fillMaxWidth()) {

            // 1) 기본 + 커스텀 전체 리스트 (전부 스와이프 삭제 가능)
            chantLabels.forEach { label ->
                val checked = (label == currentLabel)

                SwipeDeleteChantType(
                    label = label,
                    checked = checked,
                    enabled = canChangeType,
                    onClick = {
                        if (!canChangeType) return@SwipeDeleteChantType

                        val baseType = baseLabelToType[label]
                        if (baseType != null) {
                            // 기본 유형 선택
                            lastCustomLabel = null
                            onPickType(baseType)
                            onCustomChange("")
                        } else {
                            // 커스텀 유형 선택
                            lastCustomLabel = label
                            onPickType(ChantType.CUSTOM)
                            onCustomChange(label)   // 음성 인식 라벨용
                        }
                    },
                    onDelete = {
                        if (!canChangeType) return@SwipeDeleteChantType

                        // 현재 선택된 라벨을 지우는 경우 → "직접 입력" 모드로 돌려놓기
                        if (label == currentLabel) {
                            lastCustomLabel = null
                            onPickType(ChantType.CUSTOM)
                            onCustomChange("")
                        }
                        chantLabels.remove(label)
                    })
            }

            // 2) “직접 입력” 행
            ChantTypeRow(
                label = "직접 입력", checked = directChecked, enabled = canChangeType
            ) {
                if (!canChangeType) return@ChantTypeRow
                lastCustomLabel = null           // 새 직접입력 모드
                onPickType(ChantType.CUSTOM)
                onCustomChange("")               // EditText 비우기
            }

            // 3) 직접 입력 텍스트 + “추가” 버튼
            //   → lastCustomLabel 이 null일 때만 보여줌
            if (type == ChantType.CUSTOM && lastCustomLabel == null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 36.dp, top = 4.dp)
                ) {
                    OutlinedTextField(
                        value = customText,
                        onValueChange = onCustomChange,
                        placeholder = { Text("예) 나무 대자대비 관세음보살") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = canChangeType
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (!canChangeType) return@Button
                            val trimmed = customText.trim()
                            if (trimmed.isNotEmpty()) {
                                if (trimmed !in chantLabels) {
                                    chantLabels.add(trimmed)   // 리스트에 추가
                                }
                                // 방금 추가한 항목 선택 + 직접입력은 닫기
                                lastCustomLabel = trimmed
                                onPickType(ChantType.CUSTOM)
                                onCustomChange(trimmed)
                            }
                        }, enabled = canChangeType && customText.trim().isNotEmpty()
                    ) {
                        Text("추가")
                    }
                }
            }
        }

        // ===== 염불 시작 / 음성 인식 상태 =====
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onStartStop) {
                Text(if (running) "종료" else "염불 시작")
            }
            Spacer(Modifier.width(12.dp))
            if (running) {
                AssistChip(onClick = {}, label = { Text(if (listening) "음성 인식 중" else "대기") })
            }
        }

        if (heardText.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "인식된 문장: $heardText", style = MaterialTheme.typography.bodyMedium
            )
        }

        // ===== 카운터 영역 =====
        Spacer(Modifier.height(24.dp))

        var showStepDialog by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onMinusBig) { Text("-$bigStep") }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = onMinus1) { Text("–1") }
            Spacer(Modifier.width(24.dp))
            Text(
                "$count 회",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(24.dp))
            OutlinedButton(onClick = onPlus1) { Text("+") }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = onPlusBig) { Text("+$bigStep") }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { showStepDialog = true }) {
            Text("큰 증가 단위 설정 (현재: ${bigStep}회)")
        }

        if (showStepDialog) {
            SetStepDialog(initial = bigStep, onConfirm = { newStep ->
                onChangeBigStep(newStep)
                showStepDialog = false
            }, onDismiss = { showStepDialog = false })
        }

        // ===== 기록 (버튼/음성 모두 포함) =====
        Spacer(Modifier.height(24.dp))
        Text(
            "기록",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))

        val timeFormatter = remember {
            SimpleDateFormat("a hh시 mm분 ss초", Locale.KOREAN)
        }

        if (logs.isEmpty()) {
            Text("아직 기록이 없습니다.")
        } else {
            logs.forEach { entry ->
                val time = timeFormatter.format(Date(entry.timestamp))
                val tag = when (entry.source) {
                    CountType.VOICE -> "[음성 인식]"
                    CountType.MANUAL_SMALL,
                    CountType.MANUAL_BIG -> "[버튼 추가]"
                }
                val sign = if (entry.delta >= 0) "+" else ""
                // 1줄째: [버튼 추가] ㅇ시 ㅇ분 ㅇ초 +1
                Text(
                    "$tag $time ${sign}${entry.delta}",
                    style = MaterialTheme.typography.bodySmall
                )
                // 2줄째: -> 10회
                Text(
                    "-> ${entry.total}회",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(4.dp))
            }
        }

    }
}
