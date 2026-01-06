package com.rakibjoy.problembuddy.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rakibjoy.problembuddy.presentation.ui.screens.DetailsScreen
import com.rakibjoy.problembuddy.presentation.ui.screens.HomeScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(route = Screen.Details.route) {
            DetailsScreen(navController)
        }
    }
}
