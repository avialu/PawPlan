package com.avialu.pawplan.ui.screens.pets

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
import com.avialu.pawplan.ui.viewmodel.AddEditPetViewModel
import com.avialu.pawplan.ui.viewmodel.ProfileViewModel

@Composable
fun AddPetScreen(
    navController: NavController,
    vm: AddEditPetViewModel = viewModel(),
    profileVm: ProfileViewModel = viewModel()
) {
    val user by profileVm.user.collectAsState()
    val householdId = user?.activeHouseholdId
    val state by vm.state.collectAsState()

    // אם נשמר בהצלחה – נחזור אחורה
    LaunchedEffect(state.saved) {
        if (state.saved) navController.popBackStack()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Add Pet")
        Spacer(Modifier.height(12.dp))

        if (householdId.isNullOrBlank()) {
            Text("No household selected")
            return
        }

        OutlinedTextField(
            value = state.name,
            onValueChange = vm::setName,
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.breed,
            onValueChange = vm::setBreed,
            label = { Text("Breed (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.birthYear,
            onValueChange = vm::setBirthYear,
            label = { Text("Birth year (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { vm.saveNew(householdId) },
            enabled = !state.isLoading
        ) {
            Text(if (state.isLoading) "Saving..." else "Save")
        }

        state.error?.let {
            Spacer(Modifier.height(8.dp))
            Text("Error: $it")
        }
    }
}