// di/AppModule.kt
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
    ): ChantDb =
        Room.databaseBuilder(
            context,
            ChantDb::class.java,
            "chant-db"
        )
            .fallbackToDestructiveMigration()
            .build()


    @Provides
    @Singleton
    fun provideChantDao(db: ChantDb): ChantDao = db.chantDao()
}
