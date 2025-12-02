package com.app.practice.buddhismchanttracker.ui.settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

// ui/settings/SettingsRoute.kt
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
        onSignInFirebase = { email, password ->
            vm.onClickSignInFirebase(email, password)
        },
        onSignUpFirebase = { email, password ->
            vm.onClickSignUpFirebase(email, password)
        },
        onSignOut = vm::onClickSignOut
    )
}
