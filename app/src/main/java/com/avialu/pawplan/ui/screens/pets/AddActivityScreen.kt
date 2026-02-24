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
import com.avialu.pawplan.ui.viewmodel.AddActivityViewModel
import com.avialu.pawplan.ui.viewmodel.ProfileViewModel

@Composable
fun AddActivityScreen(
    navController: NavController,
    petId: String,
    vm: AddActivityViewModel = viewModel(),
    profileVm: ProfileViewModel = viewModel()
) {
    val user by profileVm.user.collectAsState()
    val householdId = user?.activeHouseholdId

    val state by vm.state.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) navController.popBackStack()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Add Activity")
        Spacer(Modifier.height(12.dp))

        if (householdId.isNullOrBlank()) {
            Text("No household selected")
            return
        }

        OutlinedTextField(
            value = state.type,
            onValueChange = vm::setType,
            label = { Text("Type (WALK/FEED/VET/GROOM/NOTE)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.note,
            onValueChange = vm::setNote,
            label = { Text("Note (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { vm.save(householdId, petId) },
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