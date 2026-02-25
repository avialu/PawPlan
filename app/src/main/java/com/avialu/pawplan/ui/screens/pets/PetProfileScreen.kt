package com.avialu.pawplan.ui.screens.pets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.avialu.pawplan.ui.navigation.PetsRoutes
import com.avialu.pawplan.ui.util.addMonths
import com.avialu.pawplan.ui.util.formatWhen
import com.avialu.pawplan.ui.util.petAgeText
import com.avialu.pawplan.ui.viewmodel.PetActivitiesViewModel
import com.avialu.pawplan.ui.viewmodel.PetProfileViewModel
import com.avialu.pawplan.ui.viewmodel.ProfileViewModel
import com.avialu.pawplan.ui.util.petTypeIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetProfileScreen(
    navController: NavController,
    petId: String,
    vm: PetProfileViewModel = viewModel(),
    activitiesVm: PetActivitiesViewModel = viewModel(),
    profileVm: ProfileViewModel = viewModel()
) {
    val user by profileVm.user.collectAsState()
    val householdId = user?.activeHouseholdId

    val state by vm.state.collectAsState()
    val activitiesState by activitiesVm.state.collectAsState()

    LaunchedEffect(householdId, petId) {
        if (!householdId.isNullOrBlank()) {
            vm.load(householdId, petId)
            activitiesVm.bind(householdId, petId)
        }
    }

    LaunchedEffect(state.deleted) {
        if (state.deleted) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pet Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Text("←")
                    }
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

            if (householdId.isNullOrBlank()) {
                Text("No household selected")
                return@Column
            }

            state.error?.let { Text("Error: $it", color = MaterialTheme.colorScheme.error) }

            val pet = state.pet
            if (pet == null) {
                Text(if (state.isLoading) "Loading..." else "No data")
                return@Column
            }

            // Header
            Text("${pet.name} ${petTypeIcon(pet.type)}", style = MaterialTheme.typography.headlineSmall)
            if (pet.breed.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(pet.breed, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            petAgeText(pet.birthYear)?.let {
                Spacer(Modifier.height(2.dp))
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(16.dp))

            // Last walk
            val lastWalkText = pet.lastWalkAt?.let { formatWhen(it) } ?: "Not yet"
            InfoRow(label = "Last walk", value = lastWalkText)

            // Next vaccine (every 3 months)
            val nextVaccineText = pet.lastVaccinationAt
                ?.let { formatWhen(addMonths(it, 3)) }
                ?: "Not set"
            InfoRow(label = "Next vaccination", value = nextVaccineText)

            // Next grooming (every 4 months)
            val nextGroomText = pet.lastGroomingAt
                ?.let { formatWhen(addMonths(it, 4)) }
                ?: "Not set"
            InfoRow(label = "Next grooming", value = nextGroomText)

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate(PetsRoutes.addActivity(petId)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Activity")
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = { navController.navigate(PetsRoutes.edit(petId)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Pet")
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = { vm.delete(householdId, petId) },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isLoading) "Deleting..." else "Delete Pet")
            }

            Spacer(Modifier.height(24.dp))

            // Activities (simple)
            Text("Recent activities", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            activitiesState.error?.let { Text("Error: $it", color = MaterialTheme.colorScheme.error) }

            if (activitiesState.activities.isEmpty()) {
                Text("No activities yet")
            } else {
                activitiesState.activities.take(10).forEach { a ->
                    Spacer(Modifier.height(8.dp))
                    val whenText = formatWhen(a.timestamp)
                    Text("• ${a.type} — $whenText")
                    a.note?.takeIf { it.isNotBlank() }?.let { Text("  $it") }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value)
    }
    Spacer(Modifier.height(10.dp))
}