package com.app.practice.buddhismchanttracker.data.model.auth

data class AuthUser(
    val id: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
    val provider: AuthProviderType
)
