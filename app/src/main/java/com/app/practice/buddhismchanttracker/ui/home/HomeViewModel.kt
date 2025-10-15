package com.app.practice.buddhismchanttracker.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.practice.buddhismchanttracker.data.model.ChantDb
import com.app.practice.buddhismchanttracker.data.model.ChantSession
import com.app.practice.buddhismchanttracker.data.repository.ChantRepository
import com.app.practice.buddhismchanttracker.voice.SpeechRecognizerManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.collections.sortedByDescending

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ChantRepository(ChantDb.get(app).dao())
    private val sr = SpeechRecognizerManager(app)

    private val today = LocalDate.now()
    private val ymd = today.toString()
    private val todayHuman = today.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 E요일", Locale.KOREAN))

    private val _ui = MutableStateFlow(HomeUiState(todayKorean = todayHuman))
    val ui: StateFlow<HomeUiState> = _ui.asStateFlow()

    init {
        // 오늘 세션 스트림
        viewModelScope.launch {
            repo.sessionsOfDay(today).collect { list ->
                _ui.update { it.copy(todaySessions = list.sortedByDescending { s -> s.startedAt }) }
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
            sr.listening.collect { L -> _ui.update { it.copy(listening = L) } }
        }
    }

    fun pickType(t: ChantType) = _ui.update { it.copy(type = t) }
    fun setCustom(text: String) = _ui.update { it.copy(customText = text) }

    fun inc(delta: Int) {
        val newCount = (_ui.value.count + delta).coerceAtLeast(0)
        _ui.update { it.copy(count = newCount) }
        _ui.value.running?.let { s -> viewModelScope.launch { repo.setCount(s, newCount) } }
    }
    fun dec() = inc(-1)

    fun toggleStartStop() {
        val running = _ui.value.running
        if (running == null) {
            // start
            viewModelScope.launch {
                val label = if (_ui.value.type == ChantType.CUSTOM) _ui.value.customText.trim().ifEmpty { "직접 입력" } else _ui.value.type.label
                val now = System.currentTimeMillis()
                val s = repo.startSession(label, if (_ui.value.type == ChantType.CUSTOM) _ui.value.customText.trim() else null, now, ymd)
                _ui.update { it.copy(running = s, count = 0) }
                startListeningFor(label)
            }
        } else {
            // stop
            viewModelScope.launch {
                repo.stopSession(running, System.currentTimeMillis())
                sr.stop()
                _ui.update { it.copy(running = null, listening = false) }
            }
        }
    }

    private fun startListeningFor(label: String) {
        // 키워드 후보: 선택된 라벨 + 보조 표기 몇 개
        val keys = buildList {
            add(label)
            if (label.contains("관세음보살")) add("관세음 보살")
            if (label.contains("아미타불")) add("아미타 불")
            if (label.contains("지장보살")) add("지장 보살")
        }
        sr.start(keys) { inc(1) }
    }
}