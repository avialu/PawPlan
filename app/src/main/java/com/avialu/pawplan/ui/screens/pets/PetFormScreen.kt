package com.avialu.pawplan.ui.screens.pets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.avialu.pawplan.ui.viewmodel.AddEditPetViewModel
import com.avialu.pawplan.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
            vm.loadForEdit(householdId, petId!!)
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Pet" else "Add Pet") },
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
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            if (householdId.isNullOrBlank()) {
                Text("No household selected")
                return@Column
            }

            // -------------------------
            // Pet type selector
            // -------------------------
            var expanded by remember { mutableStateOf(false) }

            val typeOptions = listOf("dog", "cat", "rabbit")

            Text("Pet type", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(6.dp))

            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(state.type.replaceFirstChar { it.uppercase() })
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    typeOptions.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                vm.setType(type)
                                expanded = false
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

            Spacer(Modifier.height(16.dp))

            // -------------------------
            // Save
            // -------------------------
            Button(
                onClick = {
                    if (isEdit) {
                        vm.saveEdit(householdId, petId!!)
                    } else {
                        vm.saveNew(householdId)
                    }
                },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (state.isLoading)
                        "Saving..."
                    else if (isEdit)
                        "Save changes"
                    else
                        "Add pet"
                )
            }

            state.error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}