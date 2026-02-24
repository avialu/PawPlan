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
import com.avialu.pawplan.ui.viewmodel.PetActivitiesViewModel
import com.avialu.pawplan.ui.viewmodel.PetProfileViewModel
import com.avialu.pawplan.ui.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Pet Profile")
        Spacer(Modifier.height(12.dp))

        if (householdId.isNullOrBlank()) {
            Text("No household selected")
            return
        }

        state.error?.let { Text("Error: $it") }

        val pet = state.pet
        if (pet == null) {
            Text(if (state.isLoading) "Loading..." else "No data")
            return
        }

        Text("Name: ${pet.name}")
        Text("Type: ${pet.type}")
        if (pet.breed.isNotBlank()) Text("Breed: ${pet.breed}")
        pet.birthYear?.let { Text("Birth year: $it") }

        Spacer(Modifier.height(16.dp))

        Button(onClick = { navController.navigate(PetsRoutes.edit(petId)) }) {
            Text("Edit")
        }

        Spacer(Modifier.height(8.dp))
        Button(onClick = { navController.navigate(PetsRoutes.addActivity(petId)) }) {
            Text("Add Activity")
        }

        Spacer(Modifier.height(8.dp))
        Button(onClick = { vm.delete(householdId, petId) }, enabled = !state.isLoading) {
            Text(if (state.isLoading) "Deleting..." else "Delete")
        }

        Spacer(Modifier.height(20.dp))

        // -------------------------
        // Activities list
        // -------------------------
        Text("Activities")
        Spacer(Modifier.height(8.dp))

        activitiesState.error?.let { Text("Error: $it") }

        if (activitiesState.activities.isEmpty()) {
            Text("No activities yet")
        } else {
            val fmt = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }

            activitiesState.activities.forEach { a ->
                val whenText = fmt.format(Date(a.timestamp))
                val noteText = a.note?.takeIf { it.isNotBlank() }

                Spacer(Modifier.height(8.dp))
                Text("• ${a.type} — $whenText")
                if (noteText != null) Text("  $noteText")
            }
        }
    }
}