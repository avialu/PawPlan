package com.avialu.pawplan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.avialu.pawplan.ui.screens.HomeScreen
import com.avialu.pawplan.ui.screens.LoginScreen
import com.avialu.pawplan.ui.screens.SplashScreen
import androidx.compose.runtime.remember
import com.avialu.pawplan.data.firebase.FirebaseProvider

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val startDestination = remember {
        if (FirebaseProvider.auth.currentUser != null) Routes.HOME else Routes.LOGIN
    }
    NavHost(
        navController = navController,
        startDestination = startDestination

    ) {
        composable(Routes.SPLASH) { SplashScreen(navController) }
        composable(Routes.LOGIN) { LoginScreen(navController) }
        composable(Routes.HOME) { HomeScreen(navController) }  }
}