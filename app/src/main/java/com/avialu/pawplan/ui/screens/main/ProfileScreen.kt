package com.avialu.pawplan.ui.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.avialu.pawplan.data.firebase.FirebaseProvider
import com.avialu.pawplan.ui.navigation.Routes
import com.avialu.pawplan.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    rootNavController: NavController,
    vm: ProfileViewModel = viewModel()
) {
    val user by vm.user.collectAsState()

    Column {
        Text("Profile")
        Text("Email: ${user?.email}")
        Text("Name: ${user?.displayName}")

        Button(onClick = {
            FirebaseProvider.auth.signOut()

            rootNavController.navigate(Routes.LOGIN) {
                popUpTo(0)   // מנקה את כל ה-backstack
            }
        }) {
            Text("Logout")
        }
    }
}