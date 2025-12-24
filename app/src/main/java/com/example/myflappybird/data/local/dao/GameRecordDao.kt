package com.example.myflappybird.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myflappybird.domain.model.GameRecord
import kotlinx.coroutines.flow.Flow


@Dao
interface GameRecordDao {

    @Insert
    suspend fun insert(record: GameRecord): Long

    @Query("SELECT * FROM game_records ORDER BY score DESC LIMIT 50")
    fun getTopRecords(): Flow<List<GameRecord>>

    @Query("SELECT * FROM game_records WHERE isSynced = 0")
    suspend fun getUnsyncedRecords(): List<GameRecord>

    @Update
    suspend fun update(record: GameRecord): Int

    @Query("DELETE FROM game_records")
    suspend fun deleteAll()

}