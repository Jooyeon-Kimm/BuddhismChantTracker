package com.app.practice.buddhismchanttracker.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ChantSession::class], version = 1, exportSchema = false)
abstract class ChantDb : RoomDatabase() {
    abstract fun dao(): ChantDao

    companion object {
        @Volatile private var chantDb: ChantDb? = null
        fun get(ctx: Context): ChantDb =
            chantDb ?: synchronized(this) {
                chantDb ?: Room.databaseBuilder(ctx, ChantDb::class.java, "chant.db").build().also { chantDb = it }
            }
    }
}