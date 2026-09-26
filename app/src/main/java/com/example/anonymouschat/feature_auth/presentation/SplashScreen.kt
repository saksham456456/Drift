package com.example.anonymouschat.feature_auth.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anonymouschat.feature_auth.domain.IdentityGenerator
import kotlinx.coroutines.delay

/**
 * The Splash / Identity Reveal screen.
 *
 * When the user opens Drift for the first time, this screen:
 * 1. Shows the "drift" logo fading in.
 * 2. Generates a fresh anonymous identity.
 * 3. Reveals their identity with a dramatic fade-in ("you are Wandering Ember").
 * 4. Automatically navigates to the Ocean after a short delay.
 */
@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    // Generate the identity once
    val identity = remember { IdentityGenerator.generateFreshIdentity() }

    // Animation states
    var showLogo by remember { mutableStateOf(false) }
    var showIdentity by remember { mutableStateOf(false) }
    var showSubtext by remember { mutableStateOf(false) }

    // Trigger animations in sequence
    LaunchedEffect(Unit) {
        showLogo = true
        delay(1200)
        showIdentity = true
        delay(1500)
        showSubtext = true
        delay(2000)
        onSplashFinished()
    }

    // Logo alpha animation
    val logoAlpha by animateFloatAsState(
        targetValue = if (showLogo) 1f else 0f,
        animationSpec = tween(1000, easing = EaseInOutSine),
        label = "logo_alpha"
    )

    // Identity alpha animation
    val identityAlpha by animateFloatAsState(
        targetValue = if (showIdentity) 1f else 0f,
        animationSpec = tween(1200, easing = EaseInOutSine),
        label = "identity_alpha"
    )

    // Subtext alpha animation
    val subtextAlpha by animateFloatAsState(
        targetValue = if (showSubtext) 1f else 0f,
        animationSpec = tween(800, easing = EaseInOutSine),
        label = "subtext_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App logo
            Text(
                text = "drift",
                color = Color(0xFF00E676),
                fontSize = 48.sp,
                fontWeight = FontWeight.Thin,
                letterSpacing = 16.sp,
                modifier = Modifier.alpha(logoAlpha)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Identity reveal
            Text(
                text = "you are",
                color = Color(0xFF666666),
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 4.sp,
                modifier = Modifier.alpha(identityAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = identity.displayName,
                color = Color(0xFFE0E0E0),
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 6.sp,
                modifier = Modifier.alpha(identityAlpha)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Subtext
            Text(
                text = "thoughts float.\nyou catch the ones that resonate.",
                color = Color(0xFF444444),
                fontSize = 13.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.alpha(subtextAlpha)
            )
        }
    }
}
