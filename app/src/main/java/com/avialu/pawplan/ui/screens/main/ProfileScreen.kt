package com.avialu.pawplan.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.avialu.pawplan.data.firebase.FirebaseProvider
import com.avialu.pawplan.ui.components.AppCard
import com.avialu.pawplan.ui.components.GradientHeader
import com.avialu.pawplan.ui.navigation.Routes
import com.avialu.pawplan.ui.util.formatActivityType
import com.avialu.pawplan.ui.util.formatWhenHour
import com.avialu.pawplan.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    rootNavController: NavController,
    vm: ProfileViewModel = viewModel()
) {
    val user by vm.user.collectAsState()
    val household by vm.household.collectAsState()
    val ownerName by vm.ownerName.collectAsState()
    val activities by vm.userActivities.collectAsState()

    val displayName = user?.displayName?.takeIf { it.isNotBlank() } ?: "User"
    val email = user?.email?.takeIf { it.isNotBlank() } ?: "-"
    val initial = displayName.trim().firstOrNull()?.uppercase() ?: "U"

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        GradientHeader(
            title = "Profile",
            subtitle = "Account & household"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                Divider()
                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Household",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = household?.name ?: "Not in a household",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (household != null) {
                    Spacer(Modifier.height(12.dp))

                    val isOwner = household?.createdBy == user?.uid

                    if (isOwner) {
                        Text(
                            text = "Join code",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = household?.joinCode ?: "-",
                            style = MaterialTheme.typography.titleMedium
                        )
                    } else {
                        Text(
                            text = "Household owner",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = ownerName ?: "Unknown",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                }

                if (!user?.activeHouseholdId.isNullOrBlank() && household != null) {
                    OutlinedButton(
                        onClick = { vm.leaveHousehold() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Leave household")
                    }
                    Spacer(Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        FirebaseProvider.auth.signOut()
                        rootNavController.navigate(Routes.LOGIN) {
                            popUpTo(0)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout")
                }
            }

            Spacer(Modifier.height(16.dp))

            AppCard(modifier = Modifier.fillMaxSize()) {
                Text("Your recent activity", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                if (activities.isEmpty()) {
                    Text(
                        text = "No activity yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(activities.take(30)) { a ->
                            val whenText = formatWhenHour(a.timestamp)
                            val actionText = when (a.type) {
                                "WALK" -> "Walked ${a.petName}"
                                "FEED" -> "Fed ${a.petName}"
                                "VACCINATION" -> "Vaccinated ${a.petName}"
                                "GROOMING" -> "Groomed ${a.petName}"
                                else -> "${formatActivityType(a.type)} • ${a.petName}"
                            }

                            Column {
                                Text(actionText)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = whenText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                a.note?.takeIf { it.isNotBlank() }?.let {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = it,
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
}