package com.avialu.pawplan.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
        topBar = { TopAppBar(title = { Text("Pets") }) },
        floatingActionButton = {
            if (!householdId.isNullOrBlank()) {
                FloatingActionButton(onClick = { navController.navigate(PetsRoutes.ADD) }) {
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
                Spacer(Modifier.height(10.dp))
                Text("Tap + to add your first pet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                return@Column
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(state.pets) { pet ->

                    val ageText = petAgeText(pet.birthYear)

                    val nextVaccine =
                        if (pet.vaccinationEnabled && pet.lastVaccinationAt != null)
                            addMonths(pet.lastVaccinationAt, pet.vaccinationEveryMonths.coerceAtLeast(1))
                        else null

                    val nextGroom =
                        if (pet.groomingEnabled && pet.lastGroomingAt != null)
                            addMonths(pet.lastGroomingAt, pet.groomingEveryMonths.coerceAtLeast(1))
                        else null

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
                            .clickable { navController.navigate(PetsRoutes.profile(pet.id)) }
                    ) {
                        Column(Modifier.padding(16.dp)) {

                            Text(
                                text = "${pet.name} ${petTypeIcon(pet.type)}",
                                style = MaterialTheme.typography.titleMedium
                            )

                            if (pet.breed.isNotBlank()) {
                                Spacer(Modifier.height(2.dp))
                                Text(pet.breed, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            ageText?.let {
                                Spacer(Modifier.height(2.dp))
                                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = if (!pet.vaccinationEnabled) "Vaccination: disabled"
                                else if (nextVaccine != null) "Next vaccination: ${formatWhen(nextVaccine)}"
                                else "Next vaccination: Not set",
                                color = if (pet.vaccinationEnabled) vaccineColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = if (!pet.groomingEnabled) "Grooming: disabled"
                                else if (nextGroom != null) "Next grooming: ${formatWhen(nextGroom)}"
                                else "Next grooming: Not set",
                                color = if (pet.groomingEnabled) groomColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}