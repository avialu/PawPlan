package com.avialu.pawplan.ui.screens.pets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.avialu.pawplan.ui.components.AppScaffold
import com.avialu.pawplan.ui.navigation.PetsRoutes
import com.avialu.pawplan.ui.util.*
import com.avialu.pawplan.ui.viewmodel.PetActivitiesViewModel
import com.avialu.pawplan.ui.viewmodel.PetProfileViewModel
import com.avialu.pawplan.ui.viewmodel.ProfileViewModel

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

    val now = System.currentTimeMillis()
    val todayStart = startOfDay(now)

    AppScaffold(
        title = "Pet Profile",
        onBack = { navController.popBackStack() }
    ) { padding ->

        if (householdId.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                Text("No household selected", modifier = Modifier.padding(16.dp))
            }
            return@AppScaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            state.error?.let {
                Text("Error: $it", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(10.dp))
            }

            val pet = state.pet
            if (pet == null) {
                Text(if (state.isLoading) "Loading..." else "No data")
                return@Column
            }

            val isDog = pet.type.lowercase() == "dog"

            // -------------------------
            // Header (NOT scrollable)
            // -------------------------
            Text(
                text = "${pet.name} ${petTypeIcon(pet.type)}",
                style = MaterialTheme.typography.headlineSmall
            )

            if (pet.breed.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(pet.breed, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            petAgeText(pet.birthYear)?.let {
                Spacer(Modifier.height(2.dp))
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(16.dp))

            // Daily counters summary
            val feedCountToday = if (pet.feedCountDayStart == todayStart) pet.feedCountToday else 0
            val feedTarget = pet.feedsPerDay.coerceIn(1, 6)

            Text(
                "Feeds today: $feedCountToday / $feedTarget",
                color = progressColor(feedCountToday, feedTarget)
            )
            pet.lastFeedByName?.takeIf { it.isNotBlank() }?.let {
                Text(
                    "Last feed by $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isDog) {
                Spacer(Modifier.height(10.dp))
                val walkCountToday = if (pet.walkCountDayStart == todayStart) pet.walkCountToday else 0
                val walkTarget = pet.walksPerDay.coerceIn(1, 6)

                Text(
                    "Walks today: $walkCountToday / $walkTarget",
                    color = progressColor(walkCountToday, walkTarget)
                )
                pet.lastWalkByName?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        "Last walk by $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Next vaccination / grooming
            Text(
                text =
                    if (!pet.vaccinationEnabled) "Vaccination: disabled"
                    else if (pet.lastVaccinationAt == null) "Next vaccination: Not set"
                    else "Next vaccination: ${formatWhen(addMonths(pet.lastVaccinationAt, pet.vaccinationEveryMonths.coerceAtLeast(1)))}"
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text =
                    if (!pet.groomingEnabled) "Grooming: disabled"
                    else if (pet.lastGroomingAt == null) "Next grooming: Not set"
                    else "Next grooming: ${formatWhen(addMonths(pet.lastGroomingAt, pet.groomingEveryMonths.coerceAtLeast(1)))}"
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate(PetsRoutes.addActivity(petId)) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Add Activity") }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = { navController.navigate(PetsRoutes.edit(petId)) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Edit Pet") }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = { vm.delete(householdId, petId) },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (state.isLoading) "Deleting..." else "Delete Pet") }

            Spacer(Modifier.height(18.dp))

            // -------------------------
            // ONLY THIS AREA SCROLLS
            // -------------------------
            Text("Recent activities", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            activitiesState.error?.let {
                Text("Error: $it", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            if (activitiesState.activities.isEmpty()) {
                Text("No activities yet")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // ✅ זה מה שעושה שרק הרשימה גוללת
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(activitiesState.activities.take(50)) { a ->
                        val whenText = formatWhenHour(a.timestamp)
                        val byName = a.createdByName?.trim().takeIf { !it.isNullOrBlank() } ?: "Unknown"

                        Text("• ${formatActivityType(a.type)} — $whenText — by $byName")

                        a.note?.takeIf { it.isNotBlank() }?.let {
                            Text("  $it", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}