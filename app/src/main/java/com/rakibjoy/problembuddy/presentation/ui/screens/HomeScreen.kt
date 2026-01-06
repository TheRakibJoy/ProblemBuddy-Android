package com.rakibjoy.problembuddy.presentation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.rakibjoy.problembuddy.presentation.viewmodels.GlobalNavigationIntent

@Composable
fun HomeScreen(navController: NavHostController, globalIntent: GlobalNavigationIntent) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "HomeScreen")

    }
}
