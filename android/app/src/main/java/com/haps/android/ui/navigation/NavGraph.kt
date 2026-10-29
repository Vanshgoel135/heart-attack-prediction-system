package com.haps.android.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.haps.android.ui.screens.*
import com.haps.android.ui.viewmodel.*

sealed class BottomNavItem(val screen: Screen, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Dashboard : BottomNavItem(Screen.Dashboard, "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
    object Predict : BottomNavItem(Screen.Prediction, "Predict", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)
    object History : BottomNavItem(Screen.History, "History", Icons.Filled.History, Icons.Outlined.History)
    object Profile : BottomNavItem(Screen.Profile, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
    object Settings : BottomNavItem(Screen.Settings, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel(),
    predictionViewModel: PredictionViewModel = hiltViewModel(),
    historyViewModel: HistoryViewModel = hiltViewModel(),
    analyticsViewModel: AnalyticsViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Predict,
        BottomNavItem.History,
        BottomNavItem.Profile,
        BottomNavItem.Settings
    )

    val showBottomBar = bottomNavItems.any { it.screen.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.screen.route) {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Splash
            composable(Screen.Splash.route) {
                val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
                SplashScreen(
                    isLoggedIn = isLoggedIn,
                    onNavigateToDashboard = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // Login
            composable(Screen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // Register
            composable(Screen.Register.route) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // Forgot Password
            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    authViewModel = authViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Dashboard
            composable(
                route = Screen.Dashboard.route,
                deepLinks = listOf(navDeepLink { uriPattern = "haps://app/dashboard" })
            ) {
                DashboardScreen(
                    predictionViewModel = predictionViewModel,
                    historyViewModel = historyViewModel,
                    onNavigateToPredict = { navController.navigate(Screen.Prediction.route) },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) },
                    onNavigateToAbout = { navController.navigate(Screen.About.route) },
                    onNavigateToPredictionDetail = { id ->
                        navController.navigate(Screen.PredictionDetail.createRoute(id))
                    }
                )
            }

            // Prediction Form
            composable(
                route = Screen.Prediction.route,
                deepLinks = listOf(navDeepLink { uriPattern = "haps://app/prediction" })
            ) {
                PredictionScreen(
                    predictionViewModel = predictionViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onPredictionResult = { id ->
                        navController.navigate(Screen.PredictionResult.createRoute(id))
                    }
                )
            }

            // Prediction Result
            composable(
                route = Screen.PredictionResult.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
                deepLinks = listOf(navDeepLink { uriPattern = "haps://app/result/{id}" })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                PredictionResultScreen(
                    predictionId = id,
                    predictionViewModel = predictionViewModel,
                    historyViewModel = historyViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDashboard = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }

            // History List
            composable(
                route = Screen.History.route,
                deepLinks = listOf(navDeepLink { uriPattern = "haps://app/history" })
            ) {
                HistoryScreen(
                    historyViewModel = historyViewModel,
                    onNavigateToDetail = { id ->
                        navController.navigate(Screen.PredictionDetail.createRoute(id))
                    },
                    onNavigateToPredict = { navController.navigate(Screen.Prediction.route) }
                )
            }

            // Prediction Detail
            composable(
                route = Screen.PredictionDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
                deepLinks = listOf(navDeepLink { uriPattern = "haps://app/prediction_detail/{id}" })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                PredictionDetailScreen(
                    predictionId = id,
                    historyViewModel = historyViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Analytics
            composable(
                route = Screen.Analytics.route,
                deepLinks = listOf(navDeepLink { uriPattern = "haps://app/analytics" })
            ) {
                AnalyticsScreen(
                    analyticsViewModel = analyticsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Profile
            composable(
                route = Screen.Profile.route,
                deepLinks = listOf(navDeepLink { uriPattern = "haps://app/profile" })
            ) {
                ProfileScreen(
                    profileViewModel = profileViewModel,
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Settings
            composable(
                route = Screen.Settings.route,
                deepLinks = listOf(navDeepLink { uriPattern = "haps://app/settings" })
            ) {
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    onNavigateToAbout = { navController.navigate(Screen.About.route) },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // About
            composable(
                route = Screen.About.route,
                deepLinks = listOf(navDeepLink { uriPattern = "haps://app/about" })
            ) {
                AboutScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
