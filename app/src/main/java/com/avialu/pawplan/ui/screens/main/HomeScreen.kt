package com.avialu.pawplan.ui.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.avialu.pawplan.data.firebase.FirebaseProvider
import com.avialu.pawplan.ui.navigation.Routes
import com.avialu.pawplan.ui.viewmodel.ProfileViewModel
import com.avialu.pawplan.ui.viewmodel.PetsViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    profileVm: ProfileViewModel = viewModel(),
    petsVm: PetsViewModel = viewModel()
) {

    val user by profileVm.user.collectAsState()
    val petsState by petsVm.state.collectAsState()

    val householdId = user?.activeHouseholdId

    // Bind pets once householdId exists
    LaunchedEffect(householdId) {
        if (!householdId.isNullOrBlank()) {
            petsVm.bind(householdId)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        Text("Home")
        Spacer(Modifier.height(8.dp))

        Text("Hello ${user?.displayName ?: ""}")
        Spacer(Modifier.height(16.dp))

        // --------------------
        // Add Pet
        // --------------------
        Text("Add Pet")
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = petsState.name,
            onValueChange = petsVm::setName,
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = petsState.breed,
            onValueChange = petsVm::setBreed,
            label = { Text("Breed (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = petsState.birthYear,
            onValueChange = petsVm::setBirthYear,
            label = { Text("Birth year (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                if (!householdId.isNullOrBlank()) {
                    petsVm.addPet(householdId)
                }
            },
            enabled = !petsState.isLoading && !householdId.isNullOrBlank()
        ) {
            Text(if (petsState.isLoading) "Adding..." else "Add")
        }

        petsState.error?.let {
            Spacer(Modifier.height(8.dp))
            Text("Error: $it")
        }

        Spacer(Modifier.height(24.dp))

        // --------------------
        // Pets List
        // --------------------
        Text("Pets")
        Spacer(Modifier.height(8.dp))

        if (petsState.pets.isEmpty()) {
            Text("No pets yet")
        } else {
            petsState.pets.forEach { pet ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("• ${pet.name} (${pet.type}) ${pet.breed}")
                    Spacer(Modifier.height(6.dp))
                    Button(
                        onClick = {
                            if (!householdId.isNullOrBlank()) {
                                petsVm.deletePet(householdId, pet.id)
                            }
                        }
                    ) {
                        Text("Delete")
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // --------------------
        // Logout
        // --------------------
        Button(onClick = {
            FirebaseProvider.auth.signOut()
            navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.HOME) { inclusive = true }
                launchSingleTop = true
            }
        }) {
            Text("Logout")
        }
    }
}