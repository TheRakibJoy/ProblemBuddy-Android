package com.rakibjoy.problembuddy

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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

private data class BottomBarItem(
    val label: String,
    val icon: ImageVector,
    val route: Any,
    val matches: (NavDestination?) -> Boolean,
)

private val bottomBarItems = listOf(
    BottomBarItem("Home", Icons.Default.Home, Home) { it?.hasRoute(Home::class) == true },
    BottomBarItem("Recommend", Icons.AutoMirrored.Filled.List, Recommend) { it?.hasRoute(Recommend::class) == true },
    BottomBarItem("Profile", Icons.Default.Person, Profile) { it?.hasRoute(Profile::class) == true },
    BottomBarItem("Settings", Icons.Default.Settings, Settings) { it?.hasRoute(Settings::class) == true },
)

private fun shouldShowBottomBar(destination: NavDestination?): Boolean {
    if (destination == null) return false
    return bottomBarItems.any { it.matches(destination) }
}

@Composable
fun ProblemBuddyNavHost(
    startDestination: Any,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar(currentDestination)) {
                NavigationBar {
                    bottomBarItems.forEach { item ->
                        val selected = item.matches(currentDestination)
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding),
        ) {
            composable<Onboarding> {
                OnboardingScreen(
                    onNavigateToHome = {
                        navController.navigate(Home) {
                            popUpTo(Onboarding) { inclusive = true }
                        }
                    },
                )
            }
            composable<Home> {
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
            composable<Recommend> { RecommendScreen() }
            composable<Train> { TrainScreen() }
            composable<Profile> { ProfileScreen() }
            composable<Settings> {
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
