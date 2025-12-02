package com.app.practice.buddhismchanttracker.data.model.chant

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ChantSession::class,
        ChantLogEntity::class,   // 로그 테이블 포함
    ],
    version = 3,
    exportSchema = false        // schemaLocation 경고 없애기
)
abstract class ChantDb : RoomDatabase() {
    abstract fun chantDao(): ChantDao
}
