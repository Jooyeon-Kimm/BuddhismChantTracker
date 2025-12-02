// ui/settings/SettingsScreen.kt
package com.app.practice.buddhismchanttracker.ui.settings

import AccountCard
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.practice.buddhismchanttracker.ui.home.ChantType

@Composable
fun SettingsScreen(
    ui: SettingsUiState,
    onAggregationChange: (StatsAggregation) -> Unit,
    onAllTypesToggle: (Boolean) -> Unit,
    onTypeChange: (ChantType?) -> Unit,
    onSignInKakao: () -> Unit,
    onSignInGoogle: () -> Unit,
    onSignInFirebase: (String, String) -> Unit,
    onSignUpFirebase: (String, String) -> Unit,   // <- 회원가입 콜백 추가
    onSignOut: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. 계정 정보
        item {
            AccountCard(
                ui = ui,
                onSignInKakao = onSignInKakao,
                onSignInGoogle = onSignInGoogle,
                onSignInFirebase = onSignInFirebase,
                onSignUpFirebase = onSignUpFirebase,   // <- 여기도 추가
                onSignOut = onSignOut
            )
        }

        // 2. 통계 필터
        item {
            StatsFilterCard(
                ui = ui,
                onAggregationChange = onAggregationChange,
                onAllTypesToggle = onAllTypesToggle,
                onTypeChange = onTypeChange
            )
        }

        // 3. 통계 그래프
        item {
            StatsChartCard(ui = ui)
        }
    }
}
