package com.app.practice.buddhismchanttracker.di

import com.app.practice.buddhismchanttracker.data.repository.AuthRepository
import com.app.practice.buddhismchanttracker.data.repository.DefaultAuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: DefaultAuthRepository
    ): AuthRepository
}
