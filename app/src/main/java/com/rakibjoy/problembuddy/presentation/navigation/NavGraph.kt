package com.rakibjoy.problembuddy.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rakibjoy.problembuddy.presentation.ui.screens.DetailsScreen
import com.rakibjoy.problembuddy.presentation.ui.screens.HomeScreen
import com.rakibjoy.problembuddy.presentation.viewmodels.GlobalNavigationIntent

@Composable
fun AppNavigation(navController: NavHostController, globalIntent: GlobalNavigationIntent) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(navController,globalIntent)
        }
        composable(route = Screen.Details.route) {
            DetailsScreen(navController,globalIntent)
        }
    }
}
