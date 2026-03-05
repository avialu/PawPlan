package com.avialu.pawplan.ui.screens.pets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.avialu.pawplan.ui.components.AppScaffold
import com.avialu.pawplan.ui.viewmodel.AddEditPetViewModel
import com.avialu.pawplan.ui.viewmodel.ProfileViewModel

@Composable
fun PetFormScreen(
    navController: NavController,
    petId: String? = null, // null = add, not null = edit
    vm: AddEditPetViewModel = viewModel(),
    profileVm: ProfileViewModel = viewModel()
) {
    val user by profileVm.user.collectAsState()
    val householdId = user?.activeHouseholdId
    val state by vm.state.collectAsState()

    val isEdit = petId != null

    // Load if editing
    LaunchedEffect(householdId, petId) {
        if (isEdit && !householdId.isNullOrBlank()) {
            vm.loadForEdit(householdId, petId)
        }
    }

    // Back on success
    LaunchedEffect(state.saved) {
        if (state.saved) navController.popBackStack()
    }

    AppScaffold(
        title = if (isEdit) "Edit Pet" else "Add Pet",
        onBack = { navController.popBackStack() }
    ) { padding ->

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            if (householdId.isNullOrBlank()) {
                Text("No household selected")
                return@Column
            }

            // -------------------------
            // Pet type selector
            // -------------------------
            var typeExpanded by remember { mutableStateOf(false) }
            val typeOptions = listOf("dog", "cat", "rabbit")

            Text("Pet type", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(6.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { typeExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(state.type.replaceFirstChar { it.uppercase() })
                }

                DropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    typeOptions.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                vm.setType(type)
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // -------------------------
            // Name
            // -------------------------
            OutlinedTextField(
                value = state.name,
                onValueChange = vm::setName,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            // -------------------------
            // Breed
            // -------------------------
            OutlinedTextField(
                value = state.breed,
                onValueChange = vm::setBreed,
                label = { Text("Breed (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            // -------------------------
            // Birth year
            // -------------------------
            OutlinedTextField(
                value = state.birthYear,
                onValueChange = vm::setBirthYear,
                label = { Text("Birth year (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(18.dp))

            // =========================
            // Daily targets
            // =========================
            Text("Daily targets", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = state.feedsPerDay,
                onValueChange = vm::setFeedsPerDay,
                label = { Text("Feeds per day (1-6)") },
                modifier = Modifier.fillMaxWidth()
            )

            if (state.type.lowercase() == "dog") {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = state.walksPerDay,
                    onValueChange = vm::setWalksPerDay,
                    label = { Text("Walks per day (1-6)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(18.dp))

            // =========================
            // Schedule
            // =========================
            Text("Schedule", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))

            // Vaccination enabled
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Checkbox(
                    checked = state.vaccinationEnabled,
                    onCheckedChange = vm::setVaccinationEnabled
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text("Vaccinations enabled")
                    Text(
                        "Repeat every N months",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = state.vaccinationEveryMonths,
                onValueChange = vm::setVaccinationEveryMonths,
                label = { Text("Vaccination interval (months)") },
                enabled = state.vaccinationEnabled,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))

            // Grooming enabled
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Checkbox(
                    checked = state.groomingEnabled,
                    onCheckedChange = vm::setGroomingEnabled
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text("Grooming enabled")
                    Text(
                        "Repeat every N months",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = state.groomingEveryMonths,
                onValueChange = vm::setGroomingEveryMonths,
                label = { Text("Grooming interval (months)") },
                enabled = state.groomingEnabled,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(18.dp))

            // -------------------------
            // Save
            // -------------------------
            Button(
                onClick = {
                    if (isEdit) vm.saveEdit(householdId, petId)
                    else vm.saveNew(householdId)
                },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when {
                        state.isLoading -> "Saving..."
                        isEdit -> "Save changes"
                        else -> "Add pet"
                    }
                )
            }

            state.error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}