package com.app.practice.buddhismchanttracker.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AccountCard(
    ui: SettingsUiState,
    onSignInKakao: () -> Unit,
    onSignInGoogle: () -> Unit,
    onSignInFirebase: () -> Unit,
    onSignOut: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "계정 정보",
                style = MaterialTheme.typography.titleMedium
            )

            if (ui.loggedIn) {
                Text("이름: ${ui.userName ?: "-"}")
                Text("이메일: ${ui.userEmail ?: "-"}")
                Text("로그인 제공자: ${ui.providerLabel ?: "-"}")

                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onSignOut) {
                    Text("로그아웃")
                }
            } else {
                Text("로그인되어 있지 않습니다.")

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onSignInKakao, modifier = Modifier.weight(1f)) {
                        Text("카카오 로그인")
                    }
                    OutlinedButton(onClick = onSignInGoogle, modifier = Modifier.weight(1f)) {
                        Text("Google 로그인")
                    }
                }
                Spacer(Modifier.height(4.dp))
                OutlinedButton(onClick = onSignInFirebase, modifier = Modifier.fillMaxWidth()) {
                    Text("Firebase (이메일/기타)")
                }
            }
        }
    }
}
