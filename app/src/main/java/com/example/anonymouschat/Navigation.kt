package com.example.anonymouschat

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.anonymouschat.feature_auth.presentation.SplashScreen
import com.example.anonymouschat.feature_chat.presentation.HomeScreen
import com.example.anonymouschat.feature_chat.presentation.ChatScreen
import com.example.anonymouschat.feature_settings.presentation.SettingsScreen

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onSplashFinished = {
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable("home") {
            HomeScreen(
                onNavigateToChat = { chatId -> navController.navigate("chat/$chatId") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("chat/{chatId}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
            ChatScreen(
                chatId = chatId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
