package com.example.myflappybird

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myflappybird.navigation.Destinations
import com.example.myflappybird.ui.game.GameScreen
import com.example.myflappybird.ui.leaderboard.LeaderboardScreen
import com.example.myflappybird.ui.menu.MenuScreen
import com.example.myflappybird.ui.profile.ProfileScreen
import com.example.myflappybird.ui.theme.MyFlappyBirdTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyFlappyBirdTheme {
                val gameViewModelFactory = AppModule.provideGameViewModelFactory()
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Destinations.MENU
                ) {
                    composable(Destinations.MENU) {
                        MenuScreen(
                            onNavigateToProfile = { navController.navigate(Destinations.PROFILE) },
                            onNavigateToGame = { navController.navigate(Destinations.GAME) },
                            onNavigateToLeaderboard = { navController.navigate(Destinations.LEADERBOARD) }
                        )
                    }
                    composable(Destinations.PROFILE) {
                        ProfileScreen()
                    }
                    composable(Destinations.GAME) {
                        GameScreen(
                            viewModelFactory = gameViewModelFactory,
                        )
                    }
                    composable(Destinations.LEADERBOARD) {
                        LeaderboardScreen()
                    }
                }
            }
        }
    }
}
