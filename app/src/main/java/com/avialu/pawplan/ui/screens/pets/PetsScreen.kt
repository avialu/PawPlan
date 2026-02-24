package com.avialu.pawplan.ui.screens.pets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.avialu.pawplan.ui.navigation.PetsRoutes
import com.avialu.pawplan.ui.viewmodel.PetsListViewModel
import com.avialu.pawplan.ui.viewmodel.ProfileViewModel

@Composable
fun PetsScreen(
    navController: NavController,
    listVm: PetsListViewModel = viewModel(),
    profileVm: ProfileViewModel = viewModel()
) {
    val user by profileVm.user.collectAsState()
    val householdId = user?.activeHouseholdId

    val state by listVm.state.collectAsState()

    LaunchedEffect(householdId) {
        if (!householdId.isNullOrBlank()) {
            listVm.bind(householdId)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Pets")
        Spacer(Modifier.height(12.dp))

        Button(onClick = { navController.navigate(PetsRoutes.ADD) }) {
            Text("Add Pet")
        }

        Spacer(Modifier.height(16.dp))

        state.error?.let { Text("Error: $it") }

        if (householdId.isNullOrBlank()) {
            Text("No household selected")
            return
        }

        if (state.pets.isEmpty()) {
            Text("No pets yet")
        } else {
            state.pets.forEach { pet ->
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { navController.navigate(PetsRoutes.profile(pet.id)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("${pet.name} (${pet.type})")
                }
            }
        }
    }
}