package com.app.practice.buddhismchanttracker.di

import com.app.practice.buddhismchanttracker.data.repository.ChantRepository
import com.app.practice.buddhismchanttracker.data.repository.ChantRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindChantRepository(
        impl: ChantRepositoryImpl
    ): ChantRepository
}
