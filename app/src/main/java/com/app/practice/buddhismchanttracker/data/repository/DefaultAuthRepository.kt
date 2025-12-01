package com.app.practice.buddhismchanttracker.data.repository

import com.app.practice.buddhismchanttracker.data.model.auth.AuthProviderType
import com.app.practice.buddhismchanttracker.data.model.auth.AuthUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.security.AuthProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultAuthRepository @Inject constructor() : AuthRepository {

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser

    override suspend fun signInWithKakao() {
        // TODO: 나중에 실제 Kakao SDK 연동으로 교체
        _currentUser.value = AuthUser(
            id = "kakao_dummy_id",
            displayName = "카카오 사용자",
            email = "kakao@example.com",
            photoUrl = null,
            provider = AuthProviderType.KAKAO
        )
    }

    override suspend fun signInWithGoogle() {
        // TODO: 나중에 실제 Google Sign-In 연동으로 교체
        _currentUser.value = AuthUser(
            id = "google_dummy_id",
            displayName = "Google 사용자",
            email = "google@example.com",
            photoUrl = null,
            provider = AuthProviderType.GOOGLE
        )
    }

    override suspend fun signInWithFirebase() {
        // TODO: 나중에 실제 Firebase Auth 연동으로 교체
        _currentUser.value = AuthUser(
            id = "firebase_dummy_id",
            displayName = "Firebase 사용자",
            email = "firebase@example.com",
            photoUrl = null,
            provider = AuthProviderType.FIREBASE
        )
    }

    override suspend fun signOut() {
        _currentUser.value = null
    }
}
