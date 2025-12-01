package com.app.practice.buddhismchanttracker.data.repository

import com.app.practice.buddhismchanttracker.data.model.auth.AuthUser
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<AuthUser?>
    suspend fun signInWithKakao()
    suspend fun signInWithGoogle()
    suspend fun signInWithFirebase()
    suspend fun signOut()
}