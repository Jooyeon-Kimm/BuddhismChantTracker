package com.app.practice.buddhismchanttracker.ui.settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsRoute(
    vm: SettingsViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()

    SettingsScreen(
        ui = ui,
        onAggregationChange = vm::setAggregation,
        onAllTypesToggle = vm::setAllTypesMode,
        onTypeChange = vm::setSelectedType,
        onSignInKakao = vm::onClickSignInKakao,
        onSignInGoogle = vm::onClickSignInGoogle,
        onSignInFirebase = vm::onClickSignInFirebase,
        onSignOut = vm::onClickSignOut
    )
}
