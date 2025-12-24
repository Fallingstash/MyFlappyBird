package com.example.myflappybird.data.local.datasource
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.myflappybird.domain.model.PlayerPreferences
import com.example.myflappybird.domain.repository.PlayerPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class PlayerPreferencesDataSource(private val dataStore: DataStore<Preferences>) : PlayerPreferencesRepository {
    private val PLAYER_NAME_KEY = stringPreferencesKey("player_name")

    override fun getSettings(): Flow<PlayerPreferences> {
        return dataStore.data
            .map { preferences ->
                val name = preferences[PLAYER_NAME_KEY] ?: "Flappy Champion"
                PlayerPreferences(playerName = name)
            }
    }

    override suspend fun saveSettings(playerName: String) {
        dataStore.edit { preferences ->
            preferences[PLAYER_NAME_KEY] = playerName
        }
    }
}