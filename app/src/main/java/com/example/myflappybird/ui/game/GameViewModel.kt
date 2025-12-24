package com.example.myflappybird.ui.game

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.myflappybird.domain.engine.GameEngine
import com.example.myflappybird.domain.engine.GameInternalState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import com.example.myflappybird.data.repository.LeaderboardRepository
import com.example.myflappybird.domain.model.GameRecord
import com.example.myflappybird.domain.repository.PlayerPreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(
    private val gameEngine: GameEngine,
    private val leaderboardRepository: LeaderboardRepository,
    private val playerPreferencesRepository: PlayerPreferencesRepository
): ViewModel() {
    private companion object {
        private const val TAG = "GameViewModel"
    }

    private val _internalState = MutableStateFlow(GameInternalState(GameUiState()))
    val uiState = _internalState
        .map { it.uiState }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = _internalState.value.uiState
        )

    init {
        // Следим за изменениями состояния игры
        viewModelScope.launch {
            _internalState.map { it.uiState.gameState }
                .collect { gameState ->
                    if (gameState is GameState.GameOver) {
                        saveScore(gameState.finalScore)
                    }
                }
        }

        // Запускаем игровой цикл при старте ViewModel?
        // Нет, только когда пользователь нажмёт StartGame
    }

    fun onEvent(event: GameEvent) {
        Log.d(TAG, "onEvent: $event")
        when (event) {
            GameEvent.StartGame -> {
                Log.d(TAG, "StartGame event received")
                Log.d(TAG, "Current state: ${_internalState.value.uiState.gameState}")

                _internalState.update { current ->
                    Log.d(TAG, "Before reset: ${current.uiState.gameState}")
                    gameEngine.resetGame(current)
                }

                _internalState.update { current ->
                    Log.d(TAG, "Before start: ${current.uiState.gameState}")
                    gameEngine.startGame(current)
                }

                Log.d(TAG, "After updates: ${_internalState.value.uiState.gameState}")
                startGameLoop()
            }
            GameEvent.Jump -> {
                _internalState.update { gameEngine.applyJump(it) }
            }
            GameEvent.Pause -> {
                _internalState.update { gameEngine.togglePause(it) }
            }
            GameEvent.Reset -> {
                _internalState.update { gameEngine.resetGame(it) }
            }
        }
    }

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError

    private suspend fun saveScore(score: Int) {
        if (score <= 0) return

        try {
            val playerName = playerPreferencesRepository.getSettings().first().playerName
            val record = GameRecord(playerName = playerName, score = score)
            leaderboardRepository.addRecord(record)
            Log.d(TAG, "Score saved: $score by $playerName")
            _saveError.value = null // Сбрасываем ошибку при успехе
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save score", e)
            _saveError.value = "Не удалось сохранить рекорд: ${e.message ?: "неизвестная ошибка"}"
        }
    }

    // Метод для очистки ошибки из UI
    fun clearSaveError() {
        _saveError.value = null
    }

    private var gameJob: Job? = null


    private fun startGameLoop() {
        gameJob?.cancel()

        gameJob = viewModelScope.launch {
            while (coroutineContext.isActive) {
                if (_internalState.value.uiState.gameState is GameState.Playing) {
                    _internalState.update { gameEngine.update(it) }
                }
                delay(16)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameJob?.cancel()
    }
}

data class GameUiState(
    val gameState: GameState = GameState.Ready,
    val score: Int = 0,
    val birdPosition: Float = 0.5f, // 0.0f - верх экрана, 1.0f - низ
    val pipes: List<PipeData> = emptyList() // данные для рисования труб
)

sealed class GameState {
    object Ready : GameState()
    data class Playing(val isPaused: Boolean = false) : GameState()
    data class GameOver(val finalScore: Int) : GameState()
}

sealed class GameEvent {
    object StartGame : GameEvent()
    object Jump : GameEvent() // тап/клик
    object Pause : GameEvent()
    object Reset : GameEvent()
}

data class PipeData(
    val id: Int,
    val x: Float, // от 0.0 (левая граница) до 1.0 (правая граница) + может быть >1.0
    val gapCenterY: Float, // центр промежутка (0.0-1.0)
    val gapHeight: Float = 0.3f, // высота промежутка
    val passed: Boolean = false // прошла ли птица
)