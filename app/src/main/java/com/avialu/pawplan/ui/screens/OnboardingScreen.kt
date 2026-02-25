package com.avialu.pawplan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.avialu.pawplan.ui.navigation.Routes
import com.avialu.pawplan.ui.viewmodel.OnboardingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    vm: OnboardingViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    // אם כבר יש joinCode שנוצר -> נציג “success card”
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Household") },
                navigationIcon = {
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

            Text("Create or join a household", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            state.createdJoinCode?.let { code ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Household created ✅", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("Join code:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(code, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Continue")
                        }
                    }
                }
                return@Column
            }

            // ---- Create ----
            Text("Create", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.householdName,
                onValueChange = vm::setHouseholdName,
                label = { Text("Household name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = vm::createHousehold,
                enabled = !state.isLoading && state.householdName.trim().isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isLoading) "Creating..." else "Create household")
            }

            Spacer(Modifier.height(20.dp))
            Divider()
            Spacer(Modifier.height(20.dp))

            // ---- Join ----
            Text("Join", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.joinCode,
                onValueChange = vm::setJoinCode,
                label = { Text("Join code") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    vm.joinHousehold()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                enabled = !state.isLoading && state.joinCode.trim().length >= 4,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isLoading) "Joining..." else "Join household")
            }

            state.error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}