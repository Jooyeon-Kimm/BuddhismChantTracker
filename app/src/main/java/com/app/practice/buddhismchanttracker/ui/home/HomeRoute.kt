import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.practice.buddhismchanttracker.ui.home.HomeScreen
import com.app.practice.buddhismchanttracker.ui.home.HomeViewModel

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
        onPlus1 = vm::incSmall,
        bigStep = ui.bigStep,
        onMinusBig = vm::decBig,
        onPlusBig = vm::incBig,
        onChangeBigStep = vm::setBigStep,
        onStartStop = vm::toggleStartStop,
        logs = ui.countLogs,

        // 로그 삭제 관련
        logDeleteMode = ui.logDeleteMode,
        selectedLogTimestamps = ui.selectedLogTimestamps,
        onToggleLogDeleteMode = vm::toggleLogDeleteMode,
        onToggleLogSelected = vm::toggleLogSelected,
        onSelectAllLogs = vm::selectAllLogs,
        onClearLogSelection = vm::clearLogSelection,
        onDeleteSelectedLogs = vm::deleteSelectedLogs,

        // 아이템 삭제(다른 리스트) 관련
        items = ui.items,
        deleteMode = ui.deleteMode,
        onToggleDeleteMode = vm::toggleDeleteMode,
        onToggleChecked = vm::toggleItemChecked,
        onRemove = vm::removeItem,

        heardText = heardText,
    )
}
