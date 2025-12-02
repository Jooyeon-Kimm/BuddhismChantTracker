package com.app.practice.buddhismchanttracker.data.repository

import com.app.practice.buddhismchanttracker.data.model.auth.AuthProviderType
import com.app.practice.buddhismchanttracker.data.model.auth.AuthUser
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    init {
        // 1) 앱 시작 시점에 현재 로그인 유저를 한 번 반영
        firebaseAuth.currentUser?.let { user ->
            _currentUser.value = user.toAuthUser()
        }

        // 2) 실시간으로 Auth 상태를 보고 싶으면, 아래 authFlow 를
        //    ViewModel 등에서 collect 하도록 빼서 쓰면 됨.
        val authFlow = callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { auth ->
                trySend(auth.currentUser)
            }
            firebaseAuth.addAuthStateListener(listener)
            awaitClose { firebaseAuth.removeAuthStateListener(listener) }
        }
        // 현재 코드는 authFlow 를 여기서 collect 하진 않고,
        // 필요할 때 ViewModel 쪽에서 사용할 수도 있음.
    }

    private fun com.google.firebase.auth.FirebaseUser.toAuthUser(): AuthUser {
        return AuthUser(
            id = uid,
            displayName = displayName,
            email = email,
            photoUrl = photoUrl?.toString(),
            provider = AuthProviderType.FIREBASE
        )
    }

    override suspend fun signInWithKakao(): Result<Unit> {
        // TODO: Kakao SDK 연동 후 accessToken -> Firebase Custom Token 교환
        return Result.failure(
            UnsupportedOperationException("Kakao 로그인은 아직 구현되지 않았습니다.")
        )
    }

    override suspend fun signInWithGoogle(): Result<Unit> {
        // TODO: Google Sign-In 에서 idToken 받아서 FirebaseAuth.signInWithCredential 사용
        return Result.failure(
            UnsupportedOperationException("Google 로그인은 아직 구현되지 않았습니다.")
        )
    }

    override suspend fun signInWithFirebase(email: String, password: String): Result<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = firebaseAuth.currentUser
            _currentUser.value = user?.toAuthUser()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithFirebase(email: String, password: String): Result<Unit> {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = firebaseAuth.currentUser
            _currentUser.value = user?.toAuthUser()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            _currentUser.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
