package com.example.myflappybird.domain.repository

import com.example.myflappybird.domain.model.PlayerPreferences
import kotlinx.coroutines.flow.Flow

interface PlayerPreferencesRepository {
    fun getSettings(): Flow<PlayerPreferences>
    suspend fun saveSettings(playerName: String)
}