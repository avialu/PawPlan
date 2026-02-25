package com.avialu.pawplan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.avialu.pawplan.ui.navigation.Routes
import com.avialu.pawplan.ui.viewmodel.AuthViewModel

private fun isValidEmail(email: String): Boolean {
    val e = email.trim()
    return e.contains("@") && e.contains(".") && e.length >= 5
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    vm: AuthViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    // ניווט רק דרך Side-effect
    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.LOGIN) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // local validation
    val email = state.email
    val password = state.password
    val displayName = state.displayName

    val emailError = email.isNotBlank() && !isValidEmail(email)
    val passwordError = password.isNotBlank() && password.length < 6
    val signInEnabled = !state.isLoading && isValidEmail(email) && password.length >= 6
    val signUpEnabled = signInEnabled && displayName.trim().length >= 2

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login") },
                navigationIcon = {
                    // אם הגעת לכאן אחרי Logout אין באמת לאן לחזור, אבל Back לא מזיק
                    IconButton(onClick = { navController.popBackStack() }) { Text("←") }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(16.dp)
        ) {

            Text("Welcome to PawPlan", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = vm::setEmail,
                label = { Text("Email") },
                isError = emailError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    when {
                        emailError -> Text("Please enter a valid email")
                        else -> Text(" ")
                    }
                }
            )

            OutlinedTextField(
                value = password,
                onValueChange = vm::setPassword,
                label = { Text("Password") },
                isError = passwordError,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    when {
                        passwordError -> Text("Password must be at least 6 characters")
                        else -> Text(" ")
                    }
                }
            )

            OutlinedTextField(
                value = displayName,
                onValueChange = vm::setDisplayName,
                label = { Text("Display name (for sign up)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text(" ") }
            )

            state.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = vm::signIn,
                enabled = signInEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isLoading) "Signing in..." else "Sign In")
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = vm::signUp,
                enabled = signUpEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isLoading) "Signing up..." else "Sign Up")
            }
        }
    }
}