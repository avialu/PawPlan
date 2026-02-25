package com.avialu.pawplan.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.avialu.pawplan.data.firebase.FirebaseProvider
import com.avialu.pawplan.ui.navigation.Routes
import com.avialu.pawplan.ui.viewmodel.ProfileViewModel

@Composable
fun SplashScreen(
    navController: NavController,
    profileVm: ProfileViewModel = viewModel()
) {
    val user by profileVm.user.collectAsState()

    LaunchedEffect(FirebaseProvider.auth.currentUser, user) {
        val dest = when {
            FirebaseProvider.auth.currentUser == null -> Routes.LOGIN
            user == null -> null // עדיין טוענים פרופיל
            else -> Routes.HOME // ✅ תמיד נכנסים ל-Main, גם בלי household
        }

        if (dest != null) {
            navController.navigate(dest) {
                popUpTo(Routes.SPLASH) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Text("Loading...")
}