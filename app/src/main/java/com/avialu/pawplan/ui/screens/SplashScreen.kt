package com.avialu.pawplan.ui.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.avialu.pawplan.ui.navigation.Routes

@Composable
fun SplashScreen(navController: NavController) {
    Button(onClick = { navController.navigate(Routes.LOGIN) }) {
        Text("Go to Login")
    }
}