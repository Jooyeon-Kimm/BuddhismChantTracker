package com.app.practice.buddhismchanttracker.di

import android.content.Context
import androidx.room.Room
import com.app.practice.buddhismchanttracker.data.model.chant.ChantDao
import com.app.practice.buddhismchanttracker.data.model.chant.ChantDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideChantDb(
        @ApplicationContext context: Context
    ): ChantDb {
        return Room.databaseBuilder(
            context,
            ChantDb::class.java,
            "chant.db"
        ).build()
    }

    @Provides
    fun provideChantDao(db: ChantDb): ChantDao = db.dao()
}
