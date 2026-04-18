package com.rakibjoy.problembuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rakibjoy.problembuddy.core.ui.components.GradientSurface
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: AppRootViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val start by viewModel.startDestination.collectAsStateWithLifecycle()
            ProblemBuddyTheme(themeMode = themeMode) {
                // Keep status bar icons legible against both light and dark surfaces.
                val view = LocalView.current
                val lightStatus = !isSystemInDarkTheme()
                if (!view.isInEditMode) {
                    SideEffect {
                        val controller = WindowCompat.getInsetsController(window, view)
                        controller.isAppearanceLightStatusBars = lightStatus
                        controller.isAppearanceLightNavigationBars = lightStatus
                    }
                }
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (val s = start) {
                        null -> BrandedSplash()
                        StartDestination.Onboarding -> ProblemBuddyNavHost(startDestination = Onboarding)
                        StartDestination.Home -> ProblemBuddyNavHost(startDestination = Home)
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun BrandedSplash() {
    GradientSurface {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(400)),
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    val primary = MaterialTheme.colorScheme.primary
                    val orb = Brush.radialGradient(
                        colors = listOf(primary.copy(alpha = 0.4f), Color.Transparent),
                    )
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(orb),
                        contentAlignment = Alignment.Center,
                    ) {
                        // decorative — app launcher icon
                        androidx.compose.foundation.Image(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "ProblemBuddy",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
