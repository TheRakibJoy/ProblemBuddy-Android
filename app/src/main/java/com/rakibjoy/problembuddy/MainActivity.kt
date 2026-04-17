package com.rakibjoy.problembuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: AppRootViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val start by viewModel.startDestination.collectAsStateWithLifecycle()
            ProblemBuddyTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (val s = start) {
                        null -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                        StartDestination.Onboarding -> ProblemBuddyNavHost(startDestination = Onboarding)
                        StartDestination.Home -> ProblemBuddyNavHost(startDestination = Home)
                    }
                }
            }
        }
    }
}
