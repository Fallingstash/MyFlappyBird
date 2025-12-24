// ui/leaderboard/LeaderboardScreen.kt
package com.example.myflappybird.ui.leaderboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myflappybird.AppModule
import com.example.myflappybird.ui.theme.MyFlappyBirdTheme

@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel = viewModel(
        factory = AppModule.provideGameViewModelFactory()
    )
) {
    val leaderboard by viewModel.leaderboard.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🏆 Таблица рекордов",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }

        error?.let { errorMessage ->
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }

        if (leaderboard.isEmpty() && !isLoading && error == null) {
            Text("Пока нет рекордов. Будь первым!", modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(leaderboard) { record ->
                    LeaderboardItem(record = record)
                }
            }
        }

        Button(
            onClick = { viewModel.loadLeaderboard() },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Обновить")
        }
    }
}

@Composable
fun LeaderboardItem(record: com.example.myflappybird.domain.model.GameRecord) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = record.playerName,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Очки: ${record.score}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "#${record.id ?: "локальный"}",
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}