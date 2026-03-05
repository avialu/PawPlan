package com.avialu.pawplan.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.avialu.pawplan.ui.navigation.PetsRoutes
import com.avialu.pawplan.ui.navigation.Routes
import com.avialu.pawplan.ui.util.addMonths
import com.avialu.pawplan.ui.util.formatWhen
import com.avialu.pawplan.ui.util.petTypeIcon
import com.avialu.pawplan.ui.util.progressColor
import com.avialu.pawplan.ui.util.startOfDay
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
            .padding(16.dp)
    ) {
        Text(
            text = "Hello ${user?.displayName ?: ""}",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(18.dp))

        if (householdId.isNullOrBlank()) {
            Text("Join or create a household to start.")
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { rootNavController.navigate(Routes.ONBOARDING) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create / Join Household")
            }
            return
        }

        Text("Daily Status", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            items(state.pets) { pet ->

                val isDog = pet.type.lowercase() == "dog"

                // FEEDS today
                val feedCountToday =
                    if (pet.feedCountDayStart == todayStart) pet.feedCountToday else 0
                val feedTarget = pet.feedsPerDay.coerceIn(1, 6)

                // WALKS today (dogs only)
                val walkCountToday =
                    if (isDog && pet.walkCountDayStart == todayStart) pet.walkCountToday else 0
                val walkTarget = if (isDog) pet.walksPerDay.coerceIn(1, 6) else 0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(PetsRoutes.profile(pet.id)) }
                ) {
                    Column(Modifier.padding(16.dp)) {

                        // Name THEN icon
                        Text(
                            text = "${pet.name} ${petTypeIcon(pet.type)}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(Modifier.height(12.dp))

                        // FEED status
                        Text(
                            text = "Feeds today: $feedCountToday / $feedTarget",
                            color = progressColor(feedCountToday, feedTarget)
                        )
                        pet.lastFeedByName?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = "Last feed by $it",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // WALK status (dogs only)
                        if (isDog) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "Walks today: $walkCountToday / $walkTarget",
                                color = progressColor(walkCountToday, walkTarget)
                            )
                            pet.lastWalkByName?.takeIf { it.isNotBlank() }?.let {
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

        Spacer(Modifier.height(16.dp))

        // Next pet to vaccinate (ONE)
        val nextVaccPet = state.pets
            .filter { it.vaccinationEnabled }
            .minByOrNull { pet ->
                val base = pet.lastVaccinationAt ?: now
                addMonths(base, pet.vaccinationEveryMonths.coerceAtLeast(1))
            }

        nextVaccPet?.let { pet ->
            val due = addMonths(
                pet.lastVaccinationAt ?: now,
                pet.vaccinationEveryMonths.coerceAtLeast(1)
            )
            Text("Next pet to vaccinate:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("${pet.name} — ${formatWhen(due)}")
            Spacer(Modifier.height(14.dp))
        }

        // Next pet to groom (ONE)
        val nextGroomPet = state.pets
            .filter { it.groomingEnabled }
            .minByOrNull { pet ->
                val base = pet.lastGroomingAt ?: now
                addMonths(base, pet.groomingEveryMonths.coerceAtLeast(1))
            }

        nextGroomPet?.let { pet ->
            val due = addMonths(
                pet.lastGroomingAt ?: now,
                pet.groomingEveryMonths.coerceAtLeast(1)
            )
            Text("Next pet to groom:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("${pet.name} — ${formatWhen(due)}")
        }
    }
}