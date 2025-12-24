package com.example.myflappybird.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_records")
data class GameRecord(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,

    val id: String? = null,           // ID с сервера
    val playerName: String,
    val score: Int,
    val dateMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)