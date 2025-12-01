package com.app.practice.buddhismchanttracker.ui.home

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * 기도유형 한 줄:
 * - 오른쪽 → 왼쪽 스와이프하면 “삭제” 버튼 노출
 * - 삭제 버튼 눌렀을 때 실제 삭제
 */
@Composable
fun SwipeDeleteChantType(
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
