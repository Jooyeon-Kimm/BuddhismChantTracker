package com.app.practice.buddhismchanttracker.data.repository

import com.app.practice.buddhismchanttracker.data.model.auth.AuthUser
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {

    // 현재 로그인한 사용자
    val currentUser: StateFlow<AuthUser?>

    // 카카오 로그인 (아직 미구현이면 Result.failure로 돌려도 됨)
    suspend fun signInWithKakao(): Result<Unit>

    // 구글 로그인
    suspend fun signInWithGoogle(): Result<Unit>

    // 이메일/비밀번호 Firebase 로그인
    suspend fun signInWithFirebase(email: String, password: String): Result<Unit>

    // Firebase 회원가입
    suspend fun signUpWithFirebase(email: String, password: String): Result<Unit>

    // 로그아웃
    suspend fun signOut(): Result<Unit>


}
