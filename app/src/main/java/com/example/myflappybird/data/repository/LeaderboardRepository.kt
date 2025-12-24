package com.example.myflappybird.data.repository

import com.example.myflappybird.data.local.dao.GameRecordDao
import com.example.myflappybird.data.remote.LeaderboardApi
import com.example.myflappybird.domain.model.GameRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class LeaderboardRepository(
    private val dao: GameRecordDao,
    private val api: LeaderboardApi
) {
    // Flow автоматически обновляет UI при изменении в БД
    val leaderboardFlow: Flow<List<GameRecord>> = dao.getTopRecords()

    suspend fun refresh() {
        try {
            val remoteRecords = api.getRecords()
            // Простая стратегия: заменяем все записи
            dao.deleteAll()
            remoteRecords.forEach { record ->
                dao.insert(record.copy(isSynced = true))
            }
        } catch (e: Exception) {
            // Можно выбросить дальше или залогировать
            throw e // По ТЗ нужно обрабатывать ошибки в UI
        }
    }

    suspend fun addRecord(record: GameRecord) {
        // 1. Сохраняем локально
        val localId = dao.insert(record.copy(isSynced = false))

        // 2. Пытаемся отправить на сервер
        try {
            val savedRecord = api.addRecord(record)
            // Обновляем локальную запись
            dao.update(
                savedRecord.copy(localId = localId, isSynced = true)
            )
        } catch (e: Exception) {
            // Оставим isSynced = false для повторной попытки
            // Можно добавить логику повторных попыток
        }
    }
}