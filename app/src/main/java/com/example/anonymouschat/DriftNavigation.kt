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
                onChatTapped = { chatId, chatName ->
                    val encodedName = java.net.URLEncoder.encode(chatName, "UTF-8")
                    // Since ChatListScreen doesn't explicitly provide chatType in this callback signature, 
                    // we'll infer it or just pass "unknown" and let ChatScreen handle it, but wait, ChatListScreen might know.
                    // The user said: "determine this from the chat name or by querying the chat type. For now, if the chatName doesn't look like a username (e.g., contains spaces or is longer than 20 chars), treat it as a group."
                    val chatType = if (chatName.contains(" ") || chatName.length > 20) "group" else "direct"
                    navController.navigate("chat/$chatId/$encodedName/$chatType")
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
                onGroupCreated = { chatId, chatName ->
                    val encodedName = java.net.URLEncoder.encode(chatName, "UTF-8")
                    navController.navigate("chat/$chatId/$encodedName/group") {
                        popUpTo("chatlist")
                    }
                }
            )
        }

        composable(
            route = "chat/{chatId}/{chatName}/{chatType}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("chatName") { type = NavType.StringType },
                navArgument("chatType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val encodedName = backStackEntry.arguments?.getString("chatName") ?: ""
            val chatName = java.net.URLDecoder.decode(encodedName, "UTF-8")
            val chatType = backStackEntry.arguments?.getString("chatType") ?: "direct"
            ChatScreen(
                chatId = chatId,
                chatName = chatName,
                chatType = chatType,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("search") {
            SearchScreen(
                onChatStarted = { chatId, chatName ->
                    val encodedName = java.net.URLEncoder.encode(chatName, "UTF-8")
                    navController.navigate("chat/$chatId/$encodedName/direct") {
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
