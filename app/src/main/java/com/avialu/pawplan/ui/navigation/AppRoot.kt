package com.avialu.pawplan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.avialu.pawplan.ui.screens.main.HomeScreen
import com.avialu.pawplan.ui.screens.LoginScreen
import com.avialu.pawplan.ui.screens.OnboardingScreen
import com.avialu.pawplan.ui.screens.SplashScreen

@Composable
fun AppRoot() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) { SplashScreen(navController) }
        composable(Routes.LOGIN) { LoginScreen(navController) }
        composable(Routes.ONBOARDING) { OnboardingScreen(navController) }
        composable(Routes.HOME) { HomeScreen(navController) }
        composable(Routes.HOME) {
            MainNavGraph(navController)
        }    }
}