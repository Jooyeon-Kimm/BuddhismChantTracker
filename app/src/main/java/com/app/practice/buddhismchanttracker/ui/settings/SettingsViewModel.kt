package com.app.practice.buddhismchanttracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.practice.buddhismchanttracker.data.model.auth.AuthProviderType
import com.app.practice.buddhismchanttracker.data.repository.AuthRepository
import com.app.practice.buddhismchanttracker.data.repository.ChantRepository
import com.app.practice.buddhismchanttracker.ui.home.ChantType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
                            AuthProviderType.KAKAO    -> "카카오"
                            AuthProviderType.GOOGLE   -> "Google"
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
        viewModelScope.launch {
            authRepo.signInWithKakao()
            // TODO: result 처리 (성공/실패)
        }
    }

    fun onClickSignInGoogle() {
        viewModelScope.launch {
            authRepo.signInWithGoogle()
            // TODO: result 처리
        }
    }

    fun onClickSignInFirebase(email: String, password: String) {
        viewModelScope.launch {
            _ui.update { it.copy(isAuthLoading = true, lastAuthError = null, lastAuthMessage = null) }

            val result = authRepo.signInWithFirebase(email.trim(), password)

            _ui.update { it.copy(isAuthLoading = false) }

            result
                .onSuccess {
                    _ui.update { it.copy(lastAuthMessage = "로그인에 성공했습니다.") }
                    // 🔹 로그인 성공 시 Cloud → Room 동기화
                    chantRepo.syncFromCloudForCurrentUser()
                }
                .onFailure { e ->
                    _ui.update {
                        it.copy(
                            lastAuthError = (e.message ?: "로그인에 실패했습니다.")
                        )
                    }
                }
        }
    }

    fun onClickSignUpFirebase(email: String, password: String) {
        viewModelScope.launch {
            _ui.update { it.copy(isAuthLoading = true, lastAuthError = null, lastAuthMessage = null) }

            val result = authRepo.signUpWithFirebase(email.trim(), password)

            _ui.update { it.copy(isAuthLoading = false) }

            result
                .onSuccess {
                    _ui.update { it.copy(lastAuthMessage = "회원가입이 완료되었습니다.") }
                    // 🔹 새 계정이니까 sync 호출해도 되고, 아니면 나중에 처음 기록 저장할 때부터 쌓이게 두어도 됨
                    chantRepo.syncFromCloudForCurrentUser()
                }
                .onFailure { e ->
                    _ui.update {
                        it.copy(
                            lastAuthError = (e.message ?: "회원가입에 실패했습니다.")
                        )
                    }
                }
        }
    }

    fun onClickSignOut() {
        viewModelScope.launch {
            val result = authRepo.signOut()
            result.onSuccess {
                _ui.update { it.copy(lastAuthMessage = "로그아웃되었습니다.", lastAuthError = null) }
            }.onFailure { e ->
                _ui.update { it.copy(lastAuthError = e.message ?: "로그아웃에 실패했습니다.") }
            }
        }
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
