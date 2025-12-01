package com.app.practice.buddhismchanttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.practice.buddhismchanttracker.ui.calendar.CalendarRoute
import com.app.practice.buddhismchanttracker.ui.home.HomeRoute
import com.app.practice.buddhismchanttracker.ui.settings.SettingsRoute
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val micPerm = android.Manifest.permission.RECORD_AUDIO
                val audioPermission = rememberPermissionState(micPerm)

                // 첫 진입시 권한 요청(필요 시)
                LaunchedEffect(Unit) {
                    if (audioPermission.status.isGranted.not()) {
                        audioPermission.launchPermissionRequest()
                    }
                }

                var tab by remember { mutableIntStateOf(0) }
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = tab == 0,
                                onClick = { tab = 0 },
                                icon = {},
                                label = { Text("홈") })
                            NavigationBarItem(
                                selected = tab == 1,
                                onClick = { tab = 1 },
                                icon = {},
                                label = { Text("달력") })
                            NavigationBarItem(
                                selected = tab == 2,
                                onClick = { tab = 2 },
                                icon = {},
                                label = { Text("계정정보") })
                        }
                    }
                ) { innerPadding ->
                    Box(Modifier.padding(innerPadding)) {
                        when (tab) {
                            0 -> HomeRoute()
                            1 -> CalendarRoute()
                            2 -> SettingsRoute()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Placeholder(text: String) {
    Surface { Text(text, modifier = Modifier.padding(24.dp)) }
}