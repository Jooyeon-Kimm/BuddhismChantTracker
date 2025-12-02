package com.app.practice.buddhismchanttracker.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.practice.buddhismchanttracker.data.repository.ChantRepository
import com.app.practice.buddhismchanttracker.voice.SpeechRecognizerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.collections.sortedByDescending

@HiltViewModel
class HomeViewModel  @Inject constructor(
    private val repo: ChantRepository,
    private val speechRecognizer: SpeechRecognizerManager
) : ViewModel() {

    val heardText = speechRecognizer.lastHeardText

    private val today = LocalDate.now()
    private val ymd = today.toString()
    private val todayDate =
        today.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 E요일", Locale.KOREAN))

    private val _ui = MutableStateFlow(HomeUiState(todayDate = todayDate))
    val ui: StateFlow<HomeUiState> = _ui.asStateFlow()

    init {
        // 오늘 세션 스트림
        viewModelScope.launch {
            repo.sessionsOfDay(today).collect { list ->
                _ui.update {
                    it.copy(
                        todaySessions = list.sortedByDescending { s -> s.startedAt }
                    )
                }
            }
        }
        // 진행 중 세션 복구
        viewModelScope.launch {
            val current = repo.currentRunningOrNull()
            _ui.update {
                it.copy(
                    running = current,
                    count = current?.count ?: 0
                )
            }
        }
        // 음성 리스닝 상태 반영
        viewModelScope.launch {
            speechRecognizer.listening.collect { L ->
                _ui.update { it.copy(listening = L) }
            }
        }

        // 인식된 음성 텍스트 표시
        viewModelScope.launch {
            speechRecognizer.lastHeardText.collect { text ->
                _ui.update { it.copy(heardText = text) }
            }
        }

        // === 오늘 날짜 로그 스트림 추가 ===
        viewModelScope.launch {
            repo.logsOfDay(today).collect { logs ->
                _ui.update { it.copy(countLogs = logs) }
            }
        }
    }

    fun pickType(t: ChantType) = _ui.update { it.copy(type = t) }
    fun setCustom(text: String) = _ui.update { it.copy(customText = text) }

    fun changeCount(
        delta: Int,
        source: CountType,
        log: Boolean = true
    ) {
        val oldCount = _ui.value.count
        val newCount = (oldCount + delta).coerceAtLeast(0)
        val now = System.currentTimeMillis()

        // UI용 엔트리 한 번 만들어두기
        val entry = CountLogEntry(
            timestamp = now,
            source = source,
            delta = delta,
            total = newCount,
            endTimestamp = null
        )

        _ui.update { state ->
            val newLogs =
                if (log) {
                    (listOf(entry) + state.countLogs).take(100)
                } else {
                    state.countLogs
                }

            state.copy(
                count = newCount,
                countLogs = newLogs
            )
        }

        // DB에 누적
        if (log) {
            viewModelScope.launch {
                repo.insertLog(entry, ymd)
            }
        }

        _ui.value.running?.let { s ->
            viewModelScope.launch { repo.setCount(s, newCount) }
        }
    }

    // 버튼은 log = true
    fun incSmall() = changeCount(+1, CountType.MANUAL_SMALL, log = true)
    fun decSmall() = changeCount(-1, CountType.MANUAL_SMALL, log = true)
    fun incBig()   = changeCount(_ui.value.bigStep, CountType.MANUAL_BIG, log = true)
    fun decBig()   = changeCount(-_ui.value.bigStep, CountType.MANUAL_BIG, log = true)

    // 음성 인식 hit는 count만 올리고 로그는 세션에서 관리
    fun hitByVoice() = changeCount(+1, CountType.VOICE, log = false)


    fun toggleStartStop() {
        val running = _ui.value.running
        if (running == null) {
            // ===== start =====
            viewModelScope.launch {
                val label =
                    if (_ui.value.type == ChantType.CUSTOM)
                        _ui.value.customText.trim().ifEmpty { "직접 입력" }
                    else
                        _ui.value.type.label

                val now = System.currentTimeMillis()

                val chantSession = repo.startSession(
                    typeLabel = label,
                    custom = if (_ui.value.type == ChantType.CUSTOM)
                        _ui.value.customText.trim()
                    else
                        null,
                    now = now,
                    ymd = ymd
                )

                // running 설정 + 현재 count를 그대로 사용
                _ui.update { state ->
                    val startCount = state.count          // 지금 화면에 보이던 count 유지
                    val voiceEntry = CountLogEntry(
                        timestamp = now,
                        source = CountType.VOICE,
                        delta = 0,
                        total = startCount,               // 세션 시작 시점의 누적값
                        endTimestamp = null
                    )
                    state.copy(
                        running = chantSession,
                        count = startCount,
                        countLogs = listOf(voiceEntry) + state.countLogs
                    )
                }

                startListeningFor(label)
            }
        } else {
        // ===== stop =====
        viewModelScope.launch {
            val endTime = System.currentTimeMillis()

            // 1) 세션 종료 + 음성 인식 중단
            repo.stopSession(running, endTime)
            speechRecognizer.stop()

            // 2) running / listening 해제
            _ui.update { it.copy(running = null, listening = false) }

            // 3) UI 로그 업데이트 + DB에 저장할 VOICE 로그 하나 뽑기
            var voiceLogToSave: CountLogEntry? = null

            _ui.update { state ->
                val logs = state.countLogs.toMutableList()
                val idx = logs.indexOfFirst {
                    it.source == CountType.VOICE && it.endTimestamp == null
                }

                if (idx != -1) {
                    val old = logs[idx]
                    val finalTotal = state.count
                    val finalDelta = finalTotal - old.total  // 세션 동안 증가한 총량

                    val updated = old.copy(
                        delta = finalDelta,
                        total = finalTotal,
                        endTimestamp = endTime
                    )
                    logs[idx] = updated

                    // ★ 이걸 DB에도 저장할 거야
                    voiceLogToSave = updated
                }

                state.copy(countLogs = logs)
            }

            // 4) VOICE 로그를 로그 테이블에도 저장
            voiceLogToSave?.let { entry ->
                // today용 ymd는 이미 ViewModel 맨 위에 있음 (ex: "2025-12-02")
                repo.insertLog(entry, ymd)
            }
        }
    }


}


    private fun startListeningFor(label: String) {
        val keywords = listOf(label)

        speechRecognizer.start(
            keywords = keywords,
            onHit = {
                // 음성으로 한 번 "인식 성공" 했을 때 +1
                hitByVoice()
            }
        )
    }


    // 입력값 변경
    fun setInputText(s: String) = _ui.update { it.copy(inputText = s) }

    // 항목 추가 (확인 버튼)
    fun addItem() {
        val t = _ui.value.inputText.trim()
        if (t.isEmpty()) return
        val newItem = ChantItem(text = t)
        _ui.update { it.copy(items = it.items + newItem, inputText = "") }
    }

    // 체크 토글
    fun toggleItemChecked(id: Long) {
        _ui.update {
            it.copy(
                items = it.items.map { item ->
                    if (item.id == id) item.copy(checked = !item.checked) else item
                }
            )
        }
    }

    // 삭제모드 토글 (삭제하기 버튼)
    fun toggleDeleteMode() = _ui.update { it.copy(deleteMode = !it.deleteMode) }

    // 개별 삭제 (삭제 버튼)
    fun removeItem(id: Long) {
        _ui.update { it.copy(items = it.items.filterNot { item -> item.id == id }) }
    }

    // +++ 증가 단위 설정
    fun setBigStep(step: Int) {
        val safe = step.coerceAtLeast(1)
        _ui.update { it.copy(bigStep = safe) }
    }

    fun toggleLogDeleteMode() {
        _ui.update { state ->
            if (state.logDeleteMode) {
                // 삭제 모드 끌 때 → 선택된 로그 실제 삭제
                val remain = state.countLogs.filterNot { log ->
                    log.timestamp in state.selectedLogTimestamps
                }
                state.copy(
                    logDeleteMode = false,
                    selectedLogTimestamps = emptySet(),
                    countLogs = remain
                )
            } else {
                // 삭제 모드 켤 때 → 선택 초기화
                state.copy(
                    logDeleteMode = true,
                    selectedLogTimestamps = emptySet()
                )
            }
        }
    }


    /// 특정 로그 체크 / 체크 해제
    fun toggleLogSelected(entry: CountLogEntry) {
        _ui.update { state ->
            val newSet = state.selectedLogTimestamps.toMutableSet()
            if (newSet.contains(entry.timestamp)) {
                newSet.remove(entry.timestamp)
            } else {
                newSet.add(entry.timestamp)
            }
            state.copy(selectedLogTimestamps = newSet)
        }
    }

    // 전체 선택
    fun selectAllLogs() {
        val allTs = _ui.value.countLogs.map { it.timestamp }.toSet()
        _ui.update { state ->
            state.copy(selectedLogTimestamps = allTs)
        }
    }

    // 전체 선택 해제
    fun clearLogSelection() {
        _ui.update { state ->
            state.copy(selectedLogTimestamps = emptySet())
        }
    }
    // 선택된 로그 실제 삭제
    fun deleteSelectedLogs() {
        val toDelete = _ui.value.selectedLogTimestamps
        if (toDelete.isEmpty()) return

        viewModelScope.launch {
            repo.deleteLogsByTimestamps(toDelete)
            // 삭제 후 UI 상태 정리
            _ui.update { state ->
                state.copy(
                    logDeleteMode = false,
                    selectedLogTimestamps = emptySet()
                )
            }
        }
    }

}
