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
    }

    fun pickType(t: ChantType) = _ui.update { it.copy(type = t) }
    fun setCustom(text: String) = _ui.update { it.copy(customText = text) }

    fun changeCount(delta: Int, source: CountType) {
        val oldCount = _ui.value.count
        val newCount = (oldCount + delta).coerceAtLeast(0)

        val now = System.currentTimeMillis()

        val entry = CountLogEntry(
            timestamp = now,
            source = source,
            delta = delta,
            total = newCount
        )

        _ui.update { state ->
            state.copy(
                count = newCount,
                // 새 로그를 맨 앞에 추가 → 최신이 맨 위
                countLogs = (listOf(entry) + state.countLogs).take(100)
            )
        }

        // running 세션 있으면 DB에 카운트 반영
        _ui.value.running?.let { s ->
            viewModelScope.launch { repo.setCount(s, newCount) }
        }
    }

    // 편의 함수들
    fun incSmall() = changeCount(+1, CountType.MANUAL_SMALL)
    fun decSmall() = changeCount(-1, CountType.MANUAL_SMALL)
    fun incBig()  = changeCount(_ui.value.bigStep, CountType.MANUAL_BIG)
    fun decBig()  = changeCount(-_ui.value.bigStep, CountType.MANUAL_BIG)
    fun hitByVoice() = changeCount(+1, CountType.VOICE)




    fun toggleStartStop() {
        val running = _ui.value.running
        if (running == null) {
            // start
            viewModelScope.launch {
                val label =
                    if (_ui.value.type == ChantType.CUSTOM)
                        _ui.value.customText.trim().ifEmpty { "직접 입력" }
                    else
                        _ui.value.type.label

                Log.d("HomeViewModel", "Start Listening = [$label]")

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
                _ui.update { it.copy(running = chantSession, count = 0) }
                startListeningFor(label)
            }
        } else {
            // stop
            viewModelScope.launch {
                repo.stopSession(running, System.currentTimeMillis())
                speechRecognizer.stop()
                _ui.update { it.copy(running = null, listening = false) }
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
}
