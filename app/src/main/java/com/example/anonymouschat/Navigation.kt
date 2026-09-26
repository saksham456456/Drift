package com.example.anonymouschat

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.anonymouschat.feature_auth.presentation.SplashScreen
import com.example.anonymouschat.feature_ocean.presentation.OceanScreen
import com.example.anonymouschat.feature_pocket.presentation.PocketScreen
import com.example.anonymouschat.feature_settings.presentation.SettingsScreen
import com.example.anonymouschat.feature_sky.presentation.SkyScreen

/**
 * Root navigation graph for Drift.
 *
 * Flow: Splash (identity reveal) → Ocean (drift feed) → Pocket (caught drift chat)
 *                                                      → Sky (constellations)
 *                                                      → Settings
 */
@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {

        // Identity reveal on first launch
        composable("splash") {
            SplashScreen(onSplashFinished = {
                navController.navigate("ocean") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }

        // The Ocean — main drift feed
        composable("ocean") {
            OceanScreen(
                onDriftCaught = { driftId ->
                    navController.navigate("pocket/$driftId")
                },
                onNavigateToSky = {
                    navController.navigate("sky")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }

        // A Pocket — ephemeral private chat
        composable("pocket/{driftId}") { backStackEntry ->
            val driftId = backStackEntry.arguments?.getString("driftId") ?: return@composable
            PocketScreen(driftId = driftId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // The Sky — constellation management
        composable("sky") {
            SkyScreen(
                onNavigateBack = { navController.popBackStack() },
                onConstellationTapped = { constellationId ->
                    // TODO: Create a pocket from constellation signal
                }
            )
        }

        // Settings
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
