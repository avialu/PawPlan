package com.avialu.pawplan.ui.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.avialu.pawplan.ui.viewmodel.HomeViewModel
import com.avialu.pawplan.ui.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavController, // נשאיר כדי לא לשבור את MainNavGraph, למרות שלא חייב כרגע
    profileVm: ProfileViewModel = viewModel(),
    homeVm: HomeViewModel = viewModel()
) {
    val user by profileVm.user.collectAsState()
    val householdId = user?.activeHouseholdId
    val state by homeVm.state.collectAsState()

    LaunchedEffect(householdId) {
        if (!householdId.isNullOrBlank()) {
            homeVm.bind(householdId)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Home Feed")
        Spacer(Modifier.height(8.dp))

        if (householdId.isNullOrBlank()) {
            Text("No household selected")
            return
        }

        state.error?.let {
            Spacer(Modifier.height(8.dp))
            Text("Error: $it")
        }

        Spacer(Modifier.height(12.dp))

        if (state.feed.isEmpty()) {
            Text("No events yet")
        } else {
            val fmt = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }

            state.feed.forEach { e ->
                val whenText = fmt.format(Date(e.timestamp))
                Spacer(Modifier.height(10.dp))
                Text("• ${e.petName}: ${e.type} — $whenText")
                e.note?.takeIf { it.isNotBlank() }?.let { note ->
                    Text("  $note")
                }
            }
        }
    }
}