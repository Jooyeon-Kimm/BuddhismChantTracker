package com.app.practice.buddhismchanttracker.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        onMinus1 = vm::decSmall,
        onPlus1  = vm::incSmall,
        bigStep  = ui.bigStep,
        onMinusBig = vm::decBig,
        onPlusBig  = vm::incBig,
        onChangeBigStep = vm::setBigStep,
        onStartStop = vm::toggleStartStop,
        logs = ui.countLogs,
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