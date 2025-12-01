package com.app.practice.buddhismchanttracker.ui.home

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HomeRoute(
    vm: HomeViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()
    val heardText by vm.heardText.collectAsState()

    HomeScreen(
        dateText = ui.todayDate,
        type = ui.type,
        onPickType = vm::pickType,
        customText = ui.customText,
        onCustomChange = vm::setCustom,
        running = ui.running != null,
        listening = ui.listening,
        count = ui.count,
        onMinus1 = vm::dec,
        onPlus1 = { vm.inc(1) },
        bigStep = ui.bigStep,
        onMinusBig = { vm.inc(-ui.bigStep) },
        onPlusBig = { vm.inc(ui.bigStep) },
        onChangeBigStep = vm::setBigStep,
        onStartStop = vm::toggleStartStop,
        logs = ui.todaySessions.map { s ->
            val sdf = SimpleDateFormat("a hh시 mm분 ss초", Locale.KOREAN)
            val start = sdf.format(Date(s.startedAt))
            val end = s.endedAt?.let { sdf.format(Date(it)) } ?: "진행 중"
            "· ${start}  -  ${end}   ${s.count}회"
        },
        inputText = ui.inputText,
        onInputChange = vm::setInputText,
        onConfirmAdd = vm::addItem,
        items = ui.items,
        deleteMode = ui.deleteMode,
        onToggleDeleteMode = vm::toggleDeleteMode,
        onToggleChecked = vm::toggleItemChecked,
        onRemove = vm::removeItem,
        heardText = heardText,
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
    onMinus1: () -> Unit,
    onPlus1: () -> Unit,
    bigStep: Int,
    onMinusBig: () -> Unit,
    onPlusBig: () -> Unit,
    onChangeBigStep: (Int) -> Unit,
    onStartStop: () -> Unit,
    logs: List<String>,
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
            dateText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))
        Text(
            "기도 유형",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))

        Column(Modifier.fillMaxWidth()) {

            // 1) 기본 + 커스텀 전체 리스트 (전부 스와이프 삭제 가능)
            chantLabels.forEach { label ->
                val checked = (label == currentLabel)

                SwipeRevealChantTypeRow(
                    label = label,
                    checked = checked,
                    enabled = canChangeType,
                    onClick = {
                        if (!canChangeType) return@SwipeRevealChantTypeRow

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
                        if (!canChangeType) return@SwipeRevealChantTypeRow

                        // 현재 선택된 라벨을 지우는 경우 → "직접 입력" 모드로 돌려놓기
                        if (label == currentLabel) {
                            lastCustomLabel = null
                            onPickType(ChantType.CUSTOM)
                            onCustomChange("")
                        }
                        chantLabels.remove(label)
                    }
                )
            }

            // 2) “직접 입력” 행
            ChantTypeRow(
                label = "직접 입력",
                checked = directChecked,
                enabled = canChangeType
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
                        },
                        enabled = canChangeType && customText.trim().isNotEmpty()
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
                AssistChip(
                    onClick = {},
                    label = { Text(if (listening) "음성 인식 중" else "대기") }
                )
            }
        }

        if (heardText.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "인식된 문장: $heardText",
                style = MaterialTheme.typography.bodyMedium
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
            StepDialog(
                initial = bigStep,
                onConfirm = { newStep ->
                    onChangeBigStep(newStep)
                    showStepDialog = false
                },
                onDismiss = { showStepDialog = false }
            )
        }

        // ===== 기록 =====
        Spacer(Modifier.height(24.dp))
        Text(
            "기록",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        if (logs.isEmpty()) {
            Text("오늘 기록이 아직 없어요.")
        } else {
            logs.forEach { line -> Text(line) }
        }

        // ===== 체크리스트 쪽은 네가 쓰던 코드 그대로 밑에 이어 붙이면 됨 =====
        // (지금 질문은 기도 유형 + 카운터 쪽이라 여기까지만 정리)
    }
}

@Composable
private fun ChantTypeRow(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { if (enabled) onClick() },
            enabled = enabled
        )
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            color = if (enabled)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun StepDialog(
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

/**
 * 기도유형 한 줄:
 * - 오른쪽 → 왼쪽 스와이프하면 “삭제” 버튼 노출
 * - 삭제 버튼 눌렀을 때 실제 삭제
 */
@Composable
private fun SwipeRevealChantTypeRow(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val density = LocalDensity.current
    val maxSwipeDp = 80.dp
    val maxSwipePx = with(density) { maxSwipeDp.toPx() }

    var offsetX by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(enabled) {
                // 염불 중에는 스와이프 막기
                if (!enabled) return@pointerInput
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        val newOffset = (offsetX + dragAmount)
                            .coerceIn(-maxSwipePx, 0f)   // 왼쪽으로만 스와이프
                        offsetX = newOffset
                    },
                    onDragEnd = {
                        // 절반 이상 밀렸으면 완전 오픈, 아니면 닫기
                        offsetX = if (offsetX < -maxSwipePx / 2f) -maxSwipePx else 0f
                    }
                )
            }
    ) {
        // 뒤에 깔리는 삭제 버튼
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(end = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (offsetX != 0f && enabled) {
                TextButton(onClick = onDelete) {
                    Text(
                        "삭제",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 실제 보이는 행
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .padding(vertical = 4.dp)
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { if (enabled) onClick() },
                enabled = enabled
            )
            Spacer(Modifier.width(8.dp))
            Text(label)
        }
    }
}
