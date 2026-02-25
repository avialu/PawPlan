package com.avialu.pawplan.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.avialu.pawplan.ui.navigation.PetsRoutes
import com.avialu.pawplan.ui.util.formatDayRelative
import com.avialu.pawplan.ui.util.petTypeIcon
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

        Spacer(Modifier.height(20.dp))

        if (householdId.isNullOrBlank()) {
            Text("Join or create a household to start.")
            return
        }

        Text("Daily Status", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.pets) { pet ->

                val feedTs = pet.lastFeedAt
                val feedIsToday =
                    feedTs?.let { startOfDay(it) == todayStart } ?: false

                val feedColor =
                    if (feedIsToday) Color(0xFF2E7D32)
                    else MaterialTheme.colorScheme.error

                val feedText =
                    feedTs?.let { formatDayRelative(it, now) } ?: "Not set"

                val isDog = pet.type.lowercase() == "dog"

                val walkCount =
                    if (isDog && pet.walkCountDayStart == todayStart)
                        pet.walkCountToday
                    else 0

                val walkColor = when {
                    walkCount <= 0 -> MaterialTheme.colorScheme.error
                    walkCount == 1 -> Color(0xFFFFC107)
                    else -> Color(0xFF2E7D32)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate(PetsRoutes.profile(pet.id))
                        }
                ) {
                    Column(Modifier.padding(16.dp)) {

                        Text(
                            "${pet.name} ${petTypeIcon(pet.type)}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (pet.breed.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                pet.breed,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // FEED
                        Text(
                            text = "Last feed: $feedText",
                            color = feedColor
                        )

                        pet.lastFeedByName?.let {
                            Text(
                                text = "by $it",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // WALK (dogs only)
                        if (isDog) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Walks today: $walkCount",
                                color = walkColor
                            )

                            pet.lastWalkByName?.let {
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
}