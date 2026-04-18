package com.rakibjoy.problembuddy

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.navigation.NavDestination as AndroidXNavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rakibjoy.problembuddy.core.ui.components.AppBottomBar
import com.rakibjoy.problembuddy.core.ui.components.NavDestination
import com.rakibjoy.problembuddy.feature.home.HomeScreen
import com.rakibjoy.problembuddy.feature.onboarding.OnboardingScreen
import com.rakibjoy.problembuddy.feature.profile.ProfileScreen
import com.rakibjoy.problembuddy.feature.recommend.RecommendScreen
import com.rakibjoy.problembuddy.feature.settings.SettingsScreen
import com.rakibjoy.problembuddy.feature.train.TrainScreen
import kotlinx.serialization.Serializable

@Serializable
object Onboarding

@Serializable
object Home

@Serializable
object Recommend

@Serializable
object Train

@Serializable
object Profile

@Serializable
object Settings

private fun AndroidXNavDestination?.toNavDestination(): NavDestination? = when {
    this == null -> null
    hasRoute(Home::class) -> NavDestination.Home
    hasRoute(Recommend::class) -> NavDestination.Recommend
    hasRoute(Train::class) -> NavDestination.Train
    hasRoute(Profile::class) -> NavDestination.Profile
    else -> null
}

private fun NavDestination.route(): Any = when (this) {
    NavDestination.Home -> Home
    NavDestination.Recommend -> Recommend
    NavDestination.Train -> Train
    NavDestination.Profile -> Profile
}

// Shared motion specs for NavHost destinations.
private const val ENTER_MS = 300
private const val EXIT_MS = 200

@Composable
fun ProblemBuddyNavHost(
    startDestination: Any,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val selectedTab = currentDestination.toNavDestination()
    val haptics = LocalHapticFeedback.current
    // Onboarding is blocking: every tab in the main app requires a handle, so
    // the bar renders but its items are disabled until the user finishes setup.
    val navEnabled = currentDestination == null ||
        !currentDestination.hasRoute(Onboarding::class)

    Scaffold(
        bottomBar = {
            AppBottomBar(
                selected = selectedTab,
                enabled = navEnabled,
                onSelect = { dest ->
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    navController.navigate(dest.route()) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding),
        ) {
            composable<Onboarding>(
                enterTransition = { fadeIn(tween(ENTER_MS)) + slideIntoContainer(SlideDirection.Start, tween(ENTER_MS)) },
                exitTransition = { fadeOut(tween(EXIT_MS)) + slideOutOfContainer(SlideDirection.Start, tween(ENTER_MS)) },
                popEnterTransition = { fadeIn(tween(ENTER_MS)) + slideIntoContainer(SlideDirection.End, tween(ENTER_MS)) },
                popExitTransition = { fadeOut(tween(EXIT_MS)) + slideOutOfContainer(SlideDirection.End, tween(ENTER_MS)) },
            ) {
                OnboardingScreen(
                    onNavigateToHome = {
                        navController.navigate(Home) {
                            popUpTo(Onboarding) { inclusive = true }
                        }
                    },
                )
            }
            composable<Home>(
                enterTransition = { fadeIn(tween(ENTER_MS)) + slideIntoContainer(SlideDirection.Start, tween(ENTER_MS)) },
                exitTransition = { fadeOut(tween(EXIT_MS)) + slideOutOfContainer(SlideDirection.Start, tween(ENTER_MS)) },
                popEnterTransition = { fadeIn(tween(ENTER_MS)) + slideIntoContainer(SlideDirection.End, tween(ENTER_MS)) },
                popExitTransition = { fadeOut(tween(EXIT_MS)) + slideOutOfContainer(SlideDirection.End, tween(ENTER_MS)) },
            ) {
                HomeScreen(
                    onNavigateToRecommend = {
                        navController.navigate(Recommend) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToTrain = { navController.navigate(Train) },
                    onNavigateToProfile = {
                        navController.navigate(Profile) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate(Settings) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable<Recommend>(
                enterTransition = { fadeIn(tween(ENTER_MS)) + slideIntoContainer(SlideDirection.Start, tween(ENTER_MS)) },
                exitTransition = { fadeOut(tween(EXIT_MS)) + slideOutOfContainer(SlideDirection.Start, tween(ENTER_MS)) },
                popEnterTransition = { fadeIn(tween(ENTER_MS)) + slideIntoContainer(SlideDirection.End, tween(ENTER_MS)) },
                popExitTransition = { fadeOut(tween(EXIT_MS)) + slideOutOfContainer(SlideDirection.End, tween(ENTER_MS)) },
            ) {
                RecommendScreen(
                    onNavigateToTrain = { navController.navigate(Train) },
                )
            }
            composable<Train>(
                enterTransition = { fadeIn(tween(ENTER_MS)) + slideIntoContainer(SlideDirection.Start, tween(ENTER_MS)) },
                exitTransition = { fadeOut(tween(EXIT_MS)) + slideOutOfContainer(SlideDirection.Start, tween(ENTER_MS)) },
                popEnterTransition = { fadeIn(tween(ENTER_MS)) + slideIntoContainer(SlideDirection.End, tween(ENTER_MS)) },
                popExitTransition = { fadeOut(tween(EXIT_MS)) + slideOutOfContainer(SlideDirection.End, tween(ENTER_MS)) },
            ) { TrainScreen() }
            composable<Profile>(
                enterTransition = { fadeIn(tween(ENTER_MS)) + slideIntoContainer(SlideDirection.Start, tween(ENTER_MS)) },
                exitTransition = { fadeOut(tween(EXIT_MS)) + slideOutOfContainer(SlideDirection.Start, tween(ENTER_MS)) },
                popEnterTransition = { fadeIn(tween(ENTER_MS)) + slideIntoContainer(SlideDirection.End, tween(ENTER_MS)) },
                popExitTransition = { fadeOut(tween(EXIT_MS)) + slideOutOfContainer(SlideDirection.End, tween(ENTER_MS)) },
            ) {
                ProfileScreen(
                    onNavigateToTrain = { navController.navigate(Train) },
                    onNavigateToSettings = {
                        navController.navigate(Settings) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable<Settings>(
                enterTransition = { fadeIn(tween(ENTER_MS)) + slideIntoContainer(SlideDirection.Start, tween(ENTER_MS)) },
                exitTransition = { fadeOut(tween(EXIT_MS)) + slideOutOfContainer(SlideDirection.Start, tween(ENTER_MS)) },
                popEnterTransition = { fadeIn(tween(ENTER_MS)) + slideIntoContainer(SlideDirection.End, tween(ENTER_MS)) },
                popExitTransition = { fadeOut(tween(EXIT_MS)) + slideOutOfContainer(SlideDirection.End, tween(ENTER_MS)) },
            ) {
                SettingsScreen(
                    onNavigateToOnboarding = {
                        navController.navigate(Onboarding) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}
