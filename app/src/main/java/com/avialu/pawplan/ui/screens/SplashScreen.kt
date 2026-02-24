package com.avialu.pawplan.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
            user?.activeHouseholdId.isNullOrBlank() -> Routes.ONBOARDING
            else -> Routes.HOME
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