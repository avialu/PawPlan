package com.avialu.pawplan.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.avialu.pawplan.data.firebase.FirebaseProvider
import com.avialu.pawplan.ui.navigation.Routes
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.avialu.pawplan.ui.viewmodel.ProfileViewModel

@Composable
fun HomeScreen(navController: NavController) {
    Column {
        Text("Home")

        Button(onClick = {
            FirebaseProvider.auth.signOut()
            navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.HOME) { inclusive = true }
            }
        }) {
            Text("Logout")
        }
    }
    val vm: ProfileViewModel = viewModel()
    val user by vm.user.collectAsState()

    Text("Hello ${user?.displayName}")
}