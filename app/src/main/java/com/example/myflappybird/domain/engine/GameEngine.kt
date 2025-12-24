package com.example.myflappybird.domain.engine

import android.util.Log
import com.example.myflappybird.ui.game.GameState
import com.example.myflappybird.ui.game.GameUiState
import com.example.myflappybird.ui.game.PipeData
import kotlin.random.Random

class GameEngine(private val config: GameConfig = GameConfig()) {
    companion object {
        private const val BIRD_X = 0.2f
        private const val PIPE_WIDTH = 0.2f

        // НОВЫЕ КОНСТАНТЫ ДЛЯ КОЛЛАЙДЕРОВ
        private const val BIRD_COLLIDER_WIDTH = 0.05f   // Уже чем труба
        private const val BIRD_COLLIDER_HEIGHT = 0.08f  // Короче птички
    }

    fun update(currentState: GameInternalState): GameInternalState {
        if (currentState.uiState.gameState !is GameState.Playing) {
            return currentState
        }

        if (currentState.uiState.gameState.isPaused) {
            return currentState
        }

        val newVelocity = currentState.birdVelocity + config.gravity
        val newPosition = currentState.uiState.birdPosition + newVelocity //состояние птицы

        val movedPipes = currentState.uiState.pipes.map { pipe ->
            pipe.copy(x = pipe.x - config.pipeSpeed)
        }
        val visiblePipes = movedPipes.filter { it.x > -0.2f } // если ушли дальше - не видно

        val newFramesCount = currentState.framesSinceLastPipe + 1 // + 1 кадр (каждое выполнение функции кадр)

        val (finalPipes, finalFramesCount) = if (newFramesCount >= config.pipeSpawnInterval) {
            val newPipe = PipeData(
                id = Random.nextInt(),
                x = 1.2f,
                gapCenterY = Random.nextFloat() * (0.7f - 0.3f) + 0.3f
            )
            (visiblePipes + newPipe) to 0
        } else {
            visiblePipes to newFramesCount
        }

        val updatedPipesWithScore = finalPipes.map { pipe ->
            // Если птичка прошла трубу
            // и ещё не отмечали прохождение
            val passed = !pipe.passed && pipe.x < BIRD_X// на этом этапе отметает УЖЕ ПРОЙДЕННЫЕ
            pipe.copy(passed = passed)
        }

        val newScore = currentState.uiState.score + updatedPipesWithScore.count {it.passed}

        val clampedPosition = newPosition.coerceIn(0f, 1f)


        val collidesWithPipe = finalPipes.any { pipe ->
            // ГРАНИЦЫ ПТИЦЫ (коллайдер)
            val birdLeft = BIRD_X - BIRD_COLLIDER_WIDTH / 2
            val birdRight = BIRD_X + BIRD_COLLIDER_WIDTH / 2
            val birdTop = clampedPosition - BIRD_COLLIDER_HEIGHT / 2
            val birdBottom = clampedPosition + BIRD_COLLIDER_HEIGHT / 2

            // ГРАНИЦЫ ТРУБЫ
            val pipeLeft = pipe.x
            val pipeRight = pipe.x + PIPE_WIDTH

            // ПРОВЕРКА ГОРИЗОНТАЛЬНОГО ПЕРЕСЕЧЕНИЯ
            val horizontalOverlap = birdRight > pipeLeft && birdLeft < pipeRight

            if (horizontalOverlap) {
                // Если есть горизонтальное пересечение, проверяем вертикальное
                val pipeGapTop = pipe.gapCenterY - pipe.gapHeight / 2
                val pipeGapBottom = pipe.gapCenterY + pipe.gapHeight / 2

                // Столкновение если птица НЕ в промежутке
                val verticalCollision = birdTop < pipeGapTop || birdBottom > pipeGapBottom
                verticalCollision
            } else {
                false
            }
        }

        val isCollided = clampedPosition == 0f || clampedPosition == 1f || collidesWithPipe // game over ?

        return if (isCollided) {
            currentState.copy(
                uiState = currentState.uiState.copy(
                    gameState = GameState.GameOver(finalScore = newScore),
                    birdPosition = clampedPosition
                ),
                birdVelocity = 0f,
                framesSinceLastPipe = 0
            )
        } else {
            currentState.copy(
                uiState = currentState.uiState.copy(
                    birdPosition = clampedPosition,
                    pipes = updatedPipesWithScore,
                    score = newScore
                ),
                birdVelocity = newVelocity,
                framesSinceLastPipe = finalFramesCount
            )
        }
    }

    fun togglePause(currentState: GameInternalState): GameInternalState {
        return when (val gameState = currentState.uiState.gameState) {
            is GameState.Playing -> {
                currentState.copy(
                    uiState = currentState.uiState.copy(
                        gameState = gameState.copy(isPaused = !gameState.isPaused)
                    )
                )
            }
            // В других состояниях ничего не меняем
            else -> currentState
        }
    }

    fun applyJump(currentState: GameInternalState): GameInternalState {
        return if (currentState.uiState.gameState is GameState.Playing) {
            currentState.copy(
                birdVelocity = config.jumpStrength
            )
        } else {
            currentState
        }
    }

    fun resetGame(currentState: GameInternalState): GameInternalState {
        return currentState.copy(
            uiState = currentState.uiState.copy(
                gameState = GameState.Ready,
                birdPosition = 0.5f
            ),
            birdVelocity = 0f,
            framesSinceLastPipe = 0
        )
    }

    fun startGame(currentState: GameInternalState): GameInternalState {
        Log.d("GameEngine", "startGame called")
        return currentState.copy(
            uiState = currentState.uiState.copy(
                gameState = GameState.Playing(),
                score = 0,
                pipes = emptyList(),
                birdPosition = 0.5f
            ),
            birdVelocity = 0f,
            framesSinceLastPipe = 0
        )
    }

}

data class GameInternalState(
    val uiState: GameUiState,
    val birdVelocity: Float = 0f,
    val framesSinceLastPipe: Int = 0
)

data class GameConfig(
    val gravity: Float = 0.0005f,
    val jumpStrength: Float = -0.01f,
    val pipeSpeed: Float = 0.008f,
    val pipeSpawnInterval: Int = 120
)