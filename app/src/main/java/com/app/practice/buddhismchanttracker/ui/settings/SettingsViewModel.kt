package com.app.practice.buddhismchanttracker.ui.settings

import androidx.lifecycle.ViewModel
import com.app.practice.buddhismchanttracker.data.model.auth.AuthProviderType
import com.app.practice.buddhismchanttracker.data.repository.AuthRepository
import com.app.practice.buddhismchanttracker.data.repository.ChantRepository
import com.app.practice.buddhismchanttracker.voice.SpeechRecognizerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.security.AuthProvider
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.app.practice.buddhismchanttracker.ui.home.ChantType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val chantRepo: ChantRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(SettingsUiState())
    val ui: StateFlow<SettingsUiState> = _ui.asStateFlow()

    init {
        // 로그인 상태 구독
        viewModelScope.launch {
            authRepo.currentUser.collectLatest { user ->
                _ui.update {
                    it.copy(
                        loggedIn = user != null,
                        userName = user?.displayName,
                        userEmail = user?.email,
                        providerLabel = when (user?.provider) {
                            AuthProviderType.KAKAO   -> "카카오"
                            AuthProviderType.GOOGLE  -> "Google"
                            AuthProviderType.FIREBASE -> "Firebase"
                            null -> null
                        }
                    )
                }
            }
        }

        // 초기 통계 로딩
        refreshStats()
    }

    fun setAggregation(aggregation: StatsAggregation) {
        _ui.update { it.copy(aggregation = aggregation) }
        refreshStats()
    }

    fun setAllTypesMode(allTypes: Boolean) {
        _ui.update { it.copy(allTypesMode = allTypes) }
        refreshStats()
    }

    fun setSelectedType(type: ChantType?) {
        _ui.update { it.copy(selectedType = type, allTypesMode = (type == null)) }
        refreshStats()
    }

    fun onClickSignInKakao() {
        viewModelScope.launch { authRepo.signInWithKakao() }
    }

    fun onClickSignInGoogle() {
        viewModelScope.launch { authRepo.signInWithGoogle() }
    }

    fun onClickSignInFirebase() {
        viewModelScope.launch { authRepo.signInWithFirebase() }
    }

    fun onClickSignOut() {
        viewModelScope.launch { authRepo.signOut() }
    }

    private fun refreshStats() {
        val state = _ui.value
        viewModelScope.launch {
            val points = chantRepo.loadStats(
                aggregation = state.aggregation,
                chantType = if (state.allTypesMode) null else state.selectedType
            )
            _ui.update { it.copy(points = points) }
        }
    }
}
