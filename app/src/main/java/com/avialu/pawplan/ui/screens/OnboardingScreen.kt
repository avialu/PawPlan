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
import com.avialu.pawplan.ui.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    navController: NavController,
    vm: OnboardingViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    // אם נוצר joinCode – מציגים אותו ולא נתקעים
    state.createdJoinCode?.let { code ->
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Household created ✅")
            Spacer(Modifier.height(8.dp))
            Text("Join code: $code")
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                    launchSingleTop = true
                }
            }) {
                Text("Continue")
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Create or Join Household")

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.householdName,
            onValueChange = vm::setHouseholdName,
            label = { Text("Household name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = vm::createHousehold,
            enabled = !state.isLoading
        ) {
            Text(if (state.isLoading) "Creating..." else "Create Household")
        }

        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = state.joinCode,
            onValueChange = vm::setJoinCode,
            label = { Text("Join code") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                vm.joinHousehold()
                // אם הצליח — activeHouseholdId יתעדכן, וה-Splash ייקח אותך בעתיד,
                // אבל כאן ננווט מיד ל-Home כדי שיהיה ברור
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                    launchSingleTop = true
                }
            },
            enabled = !state.isLoading
        ) {
            Text(if (state.isLoading) "Joining..." else "Join Household")
        }

        state.error?.let {
            Spacer(Modifier.height(12.dp))
            Text("Error: $it")
        }
    }
}