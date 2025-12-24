package com.example.myflappybird

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.myflappybird.data.local.datasource.PlayerPreferencesDataSource
import com.example.myflappybird.data.local.db.AppDatabase
import com.example.myflappybird.data.remote.LeaderboardApi
import com.example.myflappybird.data.remote.fake.FakeLeaderboardApi
import com.example.myflappybird.data.repository.LeaderboardRepository
import com.example.myflappybird.domain.engine.GameEngine
import com.example.myflappybird.domain.repository.PlayerPreferencesRepository
import com.example.myflappybird.ui.game.GameViewModel
import com.example.myflappybird.ui.leaderboard.LeaderboardViewModel
import com.example.myflappybird.ui.profile.ProfileViewModel
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.jvm.java

object AppModule {
    private lateinit var appContext: Context

    // DataStore
    private val dataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create(
            produceFile = { appContext.preferencesDataStoreFile("player_prefs") }
        )
    }

    // Репозиторий
    val playerPreferencesRepository: PlayerPreferencesRepository by lazy {
        PlayerPreferencesDataSource(dataStore)
    }

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    val appDatabase: AppDatabase by lazy {
        Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "game-db"
        ).build()
    }

    // Fake API (вместо реального сервера)
    val leaderboardApi: LeaderboardApi by lazy {
        FakeLeaderboardApi()
    }

    // Репозиторий таблицы рекордов
    val leaderboardRepository: LeaderboardRepository by lazy {
        LeaderboardRepository(
            dao = appDatabase.gameRecordDao(),
            api = leaderboardApi
        )
    }

    val gameEngine: GameEngine by lazy {
        GameEngine()
    }

    fun provideGameViewModelFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(GameViewModel::class.java) -> {
                        GameViewModel(
                            gameEngine = gameEngine,
                            leaderboardRepository = leaderboardRepository,
                            playerPreferencesRepository = playerPreferencesRepository
                        ) as T
                    }

                    modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                        ProfileViewModel(playerPreferencesRepository) as T
                    }

                    modelClass.isAssignableFrom(LeaderboardViewModel::class.java) -> {
                        LeaderboardViewModel(leaderboardRepository) as T
                    }

                    else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
                }
            }
        }
    }
}