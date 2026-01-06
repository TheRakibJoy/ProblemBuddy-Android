package com.rakibjoy.problembuddy

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.rakibjoy.problembuddy.presentation.navigation.AppNavigation
import com.rakibjoy.problembuddy.presentation.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.presentation.viewmodels.GlobalNavigationIntent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProblemBuddyTheme {
                val navController = rememberNavController()
                val globalIntent = GlobalNavigationIntent()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AppNavigation(navController = navController,globalIntent)
                    }
                }
            }
        }
    }
}
