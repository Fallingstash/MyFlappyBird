package com.example.myflappybird.ui.game

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myflappybird.R

@Composable
fun GameScreen(
    viewModelFactory: ViewModelProvider.Factory,
) {
    val viewModel: GameViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val saveError by viewModel.saveError.collectAsStateWithLifecycle()

    // Snackbar для ошибок
    val snackbarHostState = remember { SnackbarHostState() }

    val birdBitmap = ImageBitmap.imageResource(R.drawable.bird)
    val pipeTopBitmap = ImageBitmap.imageResource(R.drawable.pipe_top)
    val pipeBottomBitmap = ImageBitmap.imageResource(R.drawable.pipe_bottom)
    val backgroundBitmap = ImageBitmap.imageResource(R.drawable.background)

    LaunchedEffect(saveError) {
        saveError?.let { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short,
                actionLabel = "OK"
            )
            // После показа сбрасываем ошибку
            viewModel.clearSaveError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF39137A)) // ← ТВЁРДЫЙ ФОН ЗДЕСЬ!
            .pointerInput(Unit) {
                detectTapGestures {
                    viewModel.onEvent(GameEvent.Jump)
                }
            }
    ) {
        // === 1. ОБЛАКА (на фоне) ===
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Растягиваем облака на весь экран, но делаем их полупрозрачными
            drawImage(
                image = backgroundBitmap,
                dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                alpha = 0.3f // ← Прозрачность облаков
            )
        }

        // === 2. ТРУБЫ (поверх облаков) ===
        Canvas(modifier = Modifier.fillMaxSize()) {
            uiState.pipes.forEach { pipe ->
                val pipeWidth = size.width * 0.2f
                val gapHeight = size.height * pipe.gapHeight

                // Верхняя труба (фонарь)
                val topPipeHeight = size.height * pipe.gapCenterY - gapHeight / 2
                if (topPipeHeight > 0) {
                    drawImage(
                        image = pipeTopBitmap,
                        dstSize = IntSize(pipeWidth.toInt(), topPipeHeight.toInt()),
                        dstOffset = IntOffset(
                            x = (size.width * pipe.x).toInt(),
                            y = 0
                        )
                    )
                }

                // Нижняя труба (ёлка)
                val bottomPipeY = size.height * pipe.gapCenterY + gapHeight / 2
                val bottomPipeHeight = size.height - bottomPipeY
                if (bottomPipeHeight > 0) {
                    drawImage(
                        image = pipeBottomBitmap,
                        dstSize = IntSize(pipeWidth.toInt(), bottomPipeHeight.toInt()),
                        dstOffset = IntOffset(
                            x = (size.width * pipe.x).toInt(),
                            y = bottomPipeY.toInt()
                        )
                    )
                }
            }
        }



        // === 3. ПТИЦА (поверх всего) ===
        Canvas(modifier = Modifier.fillMaxSize()) {
            val birdSize = 40.dp.toPx()
            drawImage(
                image = birdBitmap,
                dstSize = IntSize(birdSize.toInt(), birdSize.toInt()),
                dstOffset = IntOffset(
                    x = (size.width * 0.2f - birdSize / 2).toInt(),
                    y = (size.height * uiState.birdPosition - birdSize / 2).toInt()
                )
            )
        }

        // === 4. UI (кнопки и счёт) - САМЫЙ ВЕРХНИЙ СЛОЙ ===
        Box(modifier = Modifier.fillMaxSize()) {
            // Счёт (показываем только во время игры и GameOver)
            if (uiState.gameState is GameState.Playing || uiState.gameState is GameState.GameOver) {
                Text(
                    text = "${uiState.score}",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 32.dp)
                )
            }

            // Кнопка "Начать игру" (только в состоянии Ready)
            if (uiState.gameState is GameState.Ready) {
                Button(
                    onClick = {
                        println("Кнопка 'Начать игру' нажата")
                        viewModel.onEvent(GameEvent.StartGame)
                    },
                    modifier = Modifier.align(Alignment.Center),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Начать игру", style = MaterialTheme.typography.titleMedium)
                }
            }

        }

        Log.d("DEBUG", "GameState: ${uiState.gameState}")
        // === 5. Game Over экран (поверх всего) ===
        if (uiState.gameState is GameState.GameOver) {
            Log.d("DEBUG", "Showing GameOverOverlay")
            GameOverOverlay(
                score = (uiState.gameState as GameState.GameOver).finalScore,
                onRestart = { viewModel.onEvent(GameEvent.StartGame) },
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun GameOverOverlay(score: Int, onRestart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Игра окончена!", color = Color.White, style = MaterialTheme.typography.headlineMedium)
            Text("Счёт: $score", color = Color.White, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRestart) {
                Text("Играть снова")
            }
        }
    }
}