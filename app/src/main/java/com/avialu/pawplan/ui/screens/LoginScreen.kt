package com.avialu.pawplan.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.avialu.pawplan.ui.navigation.Routes
import com.avialu.pawplan.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(navController: NavController, vm: AuthViewModel = viewModel()) {
    val state by vm.state.collectAsState()

    // ניתוב אוטומטי אחרי התחברות
    if (state.isLoggedIn) {
        navController.navigate(Routes.HOME) {
            popUpTo(Routes.LOGIN) { inclusive = true }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Login / Register")

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.email,
            onValueChange = vm::setEmail,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = vm::setPassword,
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.displayName,
            onValueChange = vm::setDisplayName,
            label = { Text("Display name (for sign up)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
        Button(onClick = vm::signIn, enabled = !state.isLoading) {
            Text("Sign In")
        }

        Spacer(Modifier.height(8.dp))
        Button(onClick = vm::signUp, enabled = !state.isLoading) {
            Text("Sign Up")
        }

        state.error?.let {
            Spacer(Modifier.height(12.dp))
            Text("Error: $it")
        }
    }
}