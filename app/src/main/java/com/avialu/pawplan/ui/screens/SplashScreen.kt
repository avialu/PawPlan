package com.avialu.pawplan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            user == null -> null
            else -> Routes.HOME
        }

        if (dest != null) {
            navController.navigate(dest) {
                popUpTo(Routes.SPLASH) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text("Loading...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}