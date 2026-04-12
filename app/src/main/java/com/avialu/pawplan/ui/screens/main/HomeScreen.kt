package com.avialu.pawplan.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.avialu.pawplan.ui.components.GradientHeader
import com.avialu.pawplan.ui.components.ProgressRow
import com.avialu.pawplan.ui.navigation.PetsRoutes
import com.avialu.pawplan.ui.navigation.Routes
import com.avialu.pawplan.ui.util.*
import com.avialu.pawplan.ui.viewmodel.HomeViewModel
import com.avialu.pawplan.ui.viewmodel.ProfileViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    rootNavController: NavController,
    profileVm: ProfileViewModel = viewModel(),
    vm: HomeViewModel = viewModel()
) {
    val user by profileVm.user.collectAsState()
    val householdId = user?.activeHouseholdId
    val state by vm.state.collectAsState()

    LaunchedEffect(householdId) {
        if (!householdId.isNullOrBlank()) vm.bind(householdId)
    }

    val now = System.currentTimeMillis()
    val todayStart = startOfDay(now)

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        GradientHeader(
            title = "Hello ${user?.displayName ?: ""}",
            subtitle = "Daily Status"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            if (householdId.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Join or create a household to start.")
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { rootNavController.navigate(Routes.ONBOARDING) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Create / Join Household")
                        }
                    }
                }
                return
            }

            // =========================
            // Daily Status (All pets) - aggregate card
            // =========================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                val pets = state.pets

                val completedFeedsPets = pets.count { pet ->
                    val feedCountToday =
                        if (pet.feedCountDayStart == todayStart) pet.feedCountToday else 0
                    val feedTarget = pet.feedsPerDay.coerceIn(1, 6)
                    feedCountToday == feedTarget
                }
                val totalFeedPets = pets.size

                val dogs = pets.filter { it.type.lowercase() == "dog" }
                val completedWalkPets = dogs.count { pet ->
                    val walkCountToday =
                        if (pet.walkCountDayStart == todayStart) pet.walkCountToday else 0
                    val walkTarget = pet.walksPerDay.coerceIn(1, 6)
                    walkCountToday == walkTarget
                }
                val totalWalkPets = dogs.size

                Column(Modifier.padding(16.dp)) {
                    Text("Daily Status (All pets)", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))

                    ProgressRow(
                        label = "Feeds",
                        current = completedFeedsPets,
                        target = totalFeedPets.coerceAtLeast(1),
                        color = progressColor(completedFeedsPets, totalFeedPets.coerceAtLeast(1))
                    )

                    if (dogs.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        ProgressRow(
                            label = "Walks",
                            current = completedWalkPets,
                            target = totalWalkPets.coerceAtLeast(1),
                            color = progressColor(completedWalkPets, totalWalkPets.coerceAtLeast(1))
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // =========================
            // Pets cards list (each pet)
            // =========================
            if (state.pets.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("No pets yet", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Add your first pet to start tracking daily status.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { navController.navigate(PetsRoutes.ADD) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add pet")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            } else {
                Text("Your pets", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f, fill = true),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(state.pets) { pet ->
                        val isDog = pet.type.lowercase() == "dog"

                        val feedCountToday =
                            if (pet.feedCountDayStart == todayStart) pet.feedCountToday else 0
                        val feedTarget = pet.feedsPerDay.coerceIn(1, 6)
                        val feedColor = if (feedCountToday > feedTarget) MaterialTheme.colorScheme.error else progressColor(feedCountToday, feedTarget)

                        val walkCountToday =
                            if (isDog && pet.walkCountDayStart == todayStart) pet.walkCountToday else 0
                        val walkTarget = if (isDog) pet.walksPerDay.coerceIn(1, 6) else 0

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate(PetsRoutes.profile(pet.id)) },
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    text = "${pet.name} ${petTypeIcon(pet.type)}",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(Modifier.height(12.dp))

                                ProgressRow(
                                    label = "Feeds today",
                                    current = feedCountToday,
                                    target = feedTarget,
                                    color = feedColor
                                )
                                if (feedCountToday > feedTarget) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "Too many feeds today",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }

                                pet.lastFeedByName?.takeIf { it.isNotBlank() }?.let {
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "Last feed by $it",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isDog) {
                                    Spacer(Modifier.height(14.dp))

                                    ProgressRow(
                                        label = "Walks today",
                                        current = walkCountToday,
                                        target = walkTarget,
                                        color = progressColor(walkCountToday, walkTarget)
                                    )

                                    pet.lastWalkByName?.takeIf { it.isNotBlank() }?.let {
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = "Last walk by $it",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================
            // Upcoming section - clearly separated
            // =========================
            Spacer(Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Upcoming", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))

                    val nextVaccPet = state.pets
                        .filter { it.vaccinationEnabled }
                        .minByOrNull { pet ->
                            val base = pet.lastVaccinationAt ?: now
                            addMonths(base, pet.vaccinationEveryMonths.coerceAtLeast(1))
                        }

                    val nextGroomPet = state.pets
                        .filter { it.groomingEnabled }
                        .minByOrNull { pet ->
                            val base = pet.lastGroomingAt ?: now
                            addMonths(base, pet.groomingEveryMonths.coerceAtLeast(1))
                        }

                    if (nextVaccPet == null && nextGroomPet == null) {
                        Text("No upcoming items set.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        nextVaccPet?.let { pet ->
                            val due = addMonths(
                                pet.lastVaccinationAt ?: now,
                                pet.vaccinationEveryMonths.coerceAtLeast(1)
                            )
                            Text("💉 ${pet.name} — ${formatWhen(due)}")
                            Spacer(Modifier.height(8.dp))
                        }

                        nextGroomPet?.let { pet ->
                            val due = addMonths(
                                pet.lastGroomingAt ?: now,
                                pet.groomingEveryMonths.coerceAtLeast(1)
                            )
                            Text("✂️ ${pet.name} — ${formatWhen(due)}")
                        }
                    }
                }
            }
        }
    }
}