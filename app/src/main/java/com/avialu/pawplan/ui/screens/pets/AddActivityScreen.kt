package com.avialu.pawplan.ui.screens.pets

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.avialu.pawplan.data.models.ActivityType
import com.avialu.pawplan.ui.util.formatWhen
import com.avialu.pawplan.ui.viewmodel.AddActivityViewModel
import com.avialu.pawplan.ui.viewmodel.PetsListViewModel
import com.avialu.pawplan.ui.viewmodel.ProfileViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityScreen(
    navController: NavController,
    petId: String,
    vm: AddActivityViewModel = viewModel(),
    profileVm: ProfileViewModel = viewModel(),
    petsVm: PetsListViewModel = viewModel()
) {
    val user by profileVm.user.collectAsState()
    val householdId = user?.activeHouseholdId
    val state by vm.state.collectAsState()
    val petsState by petsVm.state.collectAsState()
    val context = LocalContext.current

    // init selected pet from route (only once)
    LaunchedEffect(petId) {
        vm.initPetIfEmpty(petId)
    }

    // bind pets list
    LaunchedEffect(householdId) {
        if (!householdId.isNullOrBlank()) petsVm.bind(householdId)
    }

    // back on success
    LaunchedEffect(state.saved) {
        if (state.saved) navController.popBackStack()
    }

    // Timestamp shown
    val shownTs = state.selectedTimestamp ?: System.currentTimeMillis()
    val shownTsText = formatWhen(shownTs)

    fun openDatePicker() {
        val cal = Calendar.getInstance()
        cal.timeInMillis = shownTs

        DatePickerDialog(
            context,
            { _, y, m, d ->
                val newCal = Calendar.getInstance()
                newCal.timeInMillis = shownTs
                newCal.set(Calendar.YEAR, y)
                newCal.set(Calendar.MONTH, m)
                newCal.set(Calendar.DAY_OF_MONTH, d)
                vm.setTimestamp(newCal.timeInMillis)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun openTimePicker() {
        val cal = Calendar.getInstance()
        cal.timeInMillis = shownTs

        TimePickerDialog(
            context,
            { _, hh, mm ->
                val newCal = Calendar.getInstance()
                newCal.timeInMillis = shownTs
                newCal.set(Calendar.HOUR_OF_DAY, hh)
                newCal.set(Calendar.MINUTE, mm)
                newCal.set(Calendar.SECOND, 0)
                newCal.set(Calendar.MILLISECOND, 0)
                vm.setTimestamp(newCal.timeInMillis)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Activity") },
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
                .fillMaxWidth()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (householdId.isNullOrBlank()) {
                Text("No household selected")
                return@Column
            }

            // -------------------------
            // Pet picker (DropdownMenu anchored to button)
            // -------------------------
            var petExpanded by remember { mutableStateOf(false) }

            val selectedPetId = state.selectedPetId?.takeIf { it.isNotBlank() } ?: petId
            val selectedPetName =
                petsState.pets.firstOrNull { it.id == selectedPetId }?.name ?: "Select pet"

            Text("Pet", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(6.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { petExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedPetName)
                }

                DropdownMenu(
                    expanded = petExpanded,
                    onDismissRequest = { petExpanded = false }
                ) {
                    petsState.pets.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.name) },
                            onClick = {
                                vm.setSelectedPetId(p.id)
                                petExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // -------------------------
            // Type picker (DropdownMenu anchored to button)
            // -------------------------
            var typeExpanded by remember { mutableStateOf(false) }
            val selectedType =
                ActivityType.entries.firstOrNull { it.name == state.type } ?: ActivityType.WALK

            Text("Activity type", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(6.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { typeExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedType.label)
                }

                DropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    ActivityType.entries.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t.label) },
                            onClick = {
                                vm.setType(t.name) // WALK/VACCINATION/GROOMING/FEED/NOTE...
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // -------------------------
            // Date / Time
            // -------------------------
            Text("When", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(6.dp))
            Text(shownTsText, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { openDatePicker() },
                    modifier = Modifier.weight(1f)
                ) { Text("Pick date") }

                OutlinedButton(
                    onClick = { openTimePicker() },
                    modifier = Modifier.weight(1f)
                ) { Text("Pick time") }
            }

            Spacer(Modifier.height(6.dp))

            TextButton(onClick = { vm.setTimestamp(null) }) {
                Text("Use current time")
            }

            Spacer(Modifier.height(14.dp))

            // -------------------------
            // Note
            // -------------------------
            OutlinedTextField(
                value = state.note,
                onValueChange = vm::setNote,
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            state.error?.let {
                Spacer(Modifier.height(10.dp))
                Text("Error: $it", color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(18.dp))

            // -------------------------
            // Save
            // -------------------------
            Button(
                onClick = { vm.save(householdId, fallbackPetId = petId) },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isLoading) "Saving..." else "Save")
            }
        }
    }
}