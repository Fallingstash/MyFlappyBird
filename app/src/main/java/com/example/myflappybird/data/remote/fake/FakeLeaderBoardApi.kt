package com.example.myflappybird.data.remote.fake

import com.example.myflappybird.data.remote.LeaderboardApi
import com.example.myflappybird.domain.model.GameRecord
import kotlinx.coroutines.delay
import java.io.IOException

class FakeLeaderboardApi : LeaderboardApi {


    private val records = mutableListOf<GameRecord>()
    private var shouldFail = true

    init {
        // Начальные данные для демонстрации
        records.addAll(listOf(
            GameRecord(id = "1", playerName = "Flappy Champion", score = 150),
            GameRecord(id = "2", playerName = "Bird Master", score = 120),
            GameRecord(id = "3", playerName = "Sky King", score = 95)
        ))
    }

    override suspend fun getRecords(): List<GameRecord> {
        delay(500) // Имитация сетевой задержки
        if (shouldFail) throw IOException("Fake network error")
        return records.sortedByDescending { it.score }
    }

    override suspend fun addRecord(record: GameRecord): GameRecord {
        delay(300)
        if (shouldFail) throw IOException("Fake network error")

        val newRecord = record.copy(id = (records.size + 1).toString())
        records.add(newRecord)
        return newRecord
    }

    // Метод для тестирования ошибок
    fun setShouldFail(fail: Boolean) {
        shouldFail = fail
    }
}