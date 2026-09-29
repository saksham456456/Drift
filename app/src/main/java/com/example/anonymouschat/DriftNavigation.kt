package com.example.anonymouschat

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.anonymouschat.data.repository.AuthRepository
import com.example.anonymouschat.ui.screens.auth.LoginScreen
import com.example.anonymouschat.ui.screens.auth.SignUpScreen
import com.example.anonymouschat.ui.screens.chatlist.ChatListScreen
import com.example.anonymouschat.ui.screens.chat.ChatScreen
import com.example.anonymouschat.ui.screens.search.SearchScreen
import com.example.anonymouschat.ui.screens.settings.SettingsScreen

@Composable
fun DriftNavigation() {
    val navController = rememberNavController()
    val authRepository = AuthRepository()
    val startDest = if (authRepository.isLoggedIn()) "chatlist" else "login"

    NavHost(navController = navController, startDestination = startDest) {

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("chatlist") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate("signup")
                }
            )
        }

        composable("signup") {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate("chatlist") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("chatlist") {
            ChatListScreen(
                onChatTapped = { chatId, _ ->
                    navController.navigate("chat/$chatId")
                },
                onSearchTapped = {
                    navController.navigate("search")
                },
                onCreateGroupTapped = {
                    navController.navigate("creategroup")
                },
                onSettingsTapped = {
                    navController.navigate("settings")
                }
            )
        }
        
        composable("creategroup") {
            com.example.anonymouschat.ui.screens.group.CreateGroupScreen(
                onNavigateBack = { navController.popBackStack() },
                onGroupCreated = { chatId, _ ->
                    navController.navigate("chat/$chatId") {
                        popUpTo("chatlist")
                    }
                }
            )
        }

        composable(
            route = "chat/{chatId}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatScreen(
                chatId = chatId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("search") {
            SearchScreen(
                onChatStarted = { chatId, _ ->
                    navController.navigate("chat/$chatId") {
                        popUpTo("chatlist")
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
