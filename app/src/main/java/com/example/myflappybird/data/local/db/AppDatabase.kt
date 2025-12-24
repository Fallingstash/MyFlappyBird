package com.example.myflappybird.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myflappybird.data.local.dao.GameRecordDao
import com.example.myflappybird.domain.model.GameRecord

@Database(
    entities = [GameRecord::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameRecordDao(): GameRecordDao
}