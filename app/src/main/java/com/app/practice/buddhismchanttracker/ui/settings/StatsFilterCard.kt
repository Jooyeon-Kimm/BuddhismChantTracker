package com.app.practice.buddhismchanttracker.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.practice.buddhismchanttracker.ui.home.ChantType

@Composable
fun StatsFilterCard(
    ui: SettingsUiState,
    onAggregationChange: (StatsAggregation) -> Unit,
    onAllTypesToggle: (Boolean) -> Unit,
    onTypeChange: (ChantType?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "통계 설정",
                style = MaterialTheme.typography.titleMedium
            )

            // 전체 / 특정 타입
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("대상:", style = MaterialTheme.typography.bodyMedium)

                AssistChip(
                    label = { Text("전체") },
                    onClick = { onAllTypesToggle(true) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (ui.allTypesMode)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                AssistChip(
                    label = { Text("유형 선택") },
                    onClick = { onAllTypesToggle(false) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (!ui.allTypesMode)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                if (!ui.allTypesMode) {
                    // 간단하게 텍스트 리스트로 선택 (나중에 Dropdown 으로 바꿔도 됨)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        ui.availableTypes.forEach { t ->
                            AssistChip(
                                label = { Text(t.label) },
                                onClick = { onTypeChange(t) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor =
                                        if (ui.selectedType == t)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            // 시간 축 선택
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("시간 축:", style = MaterialTheme.typography.bodyMedium)

                listOf(
                    StatsAggregation.HOUR to "시간",
                    StatsAggregation.DAY to "일",
                    StatsAggregation.WEEK to "주",
                    StatsAggregation.MONTH to "월",
                    StatsAggregation.YEAR to "년"
                ).forEach { (agg, label) ->
                    AssistChip(
                        label = { Text(label) },
                        onClick = { onAggregationChange(agg) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor =
                                if (ui.aggregation == agg)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    }
}
