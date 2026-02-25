package com.avialu.pawplan.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.avialu.pawplan.ui.navigation.PetsRoutes
import com.avialu.pawplan.ui.navigation.Routes
import com.avialu.pawplan.ui.util.addMonths
import com.avialu.pawplan.ui.util.formatWhen
import com.avialu.pawplan.ui.util.petAgeText
import com.avialu.pawplan.ui.util.petTypeIcon
import com.avialu.pawplan.ui.viewmodel.PetsListViewModel
import com.avialu.pawplan.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetsScreen(
    navController: NavController,
    rootNavController: NavController,
    profileVm: ProfileViewModel = viewModel(),
    vm: PetsListViewModel = viewModel()
) {
    val user by profileVm.user.collectAsState()
    val householdId = user?.activeHouseholdId
    val state by vm.state.collectAsState()

    LaunchedEffect(householdId) {
        if (!householdId.isNullOrBlank()) vm.bind(householdId)
    }

    val now = System.currentTimeMillis()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pets") }
            )
        },
        floatingActionButton = {
            if (!householdId.isNullOrBlank()) {
                FloatingActionButton(
                    onClick = { navController.navigate(PetsRoutes.ADD) }
                ) {
                    Text("+")
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            if (householdId.isNullOrBlank()) {
                Text("Pets are available after you join a household.")
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { rootNavController.navigate(Routes.ONBOARDING) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create / Join Household")
                }
                return@Column
            }

            state.error?.let {
                Text("Error: $it", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
            }

            if (state.pets.isEmpty()) {
                Text("No pets yet.")
                return@Column
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(state.pets) { pet ->

                    val ageText = petAgeText(pet.birthYear)

                    val nextVaccine = pet.lastVaccinationAt?.let { addMonths(it, 3) }
                    val nextGroom = pet.lastGroomingAt?.let { addMonths(it, 4) }

                    val vaccineColor =
                        if (nextVaccine != null && nextVaccine < now)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurface

                    val groomColor =
                        if (nextGroom != null && nextGroom < now)
                            Color(0xFFFFC107)
                        else
                            MaterialTheme.colorScheme.onSurface

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(PetsRoutes.profile(pet.id))
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            // Name + icon
                            Text(
                                text = "${pet.name} ${petTypeIcon(pet.type)}",
                                style = MaterialTheme.typography.titleMedium
                            )

                            // Breed
                            if (pet.breed.isNotBlank()) {
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    pet.breed,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Age
                            ageText?.let {
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    it,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            // Vaccination
                            Text(
                                text = if (nextVaccine != null)
                                    "Next vaccination: ${formatWhen(nextVaccine)}"
                                else
                                    "Next vaccination: Not set",
                                color = vaccineColor
                            )

                            Spacer(Modifier.height(6.dp))

                            // Grooming
                            Text(
                                text = if (nextGroom != null)
                                    "Next grooming: ${formatWhen(nextGroom)}"
                                else
                                    "Next grooming: Not set",
                                color = groomColor
                            )
                        }
                    }
                }
            }
        }
    }
}