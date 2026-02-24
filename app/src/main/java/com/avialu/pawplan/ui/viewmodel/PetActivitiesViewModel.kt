package com.avialu.pawplan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avialu.pawplan.data.activities.ActivityRepository
import com.avialu.pawplan.data.models.PetActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class PetActivitiesState(
    val activities: List<PetActivity> = emptyList(),
    val error: String? = null
)

class PetActivitiesViewModel(
    private val repo: ActivityRepository = ActivityRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(PetActivitiesState())
    val state: StateFlow<PetActivitiesState> = _state

    private var boundKey: String? = null

    fun bind(householdId: String, petId: String) {
        val key = "$householdId|$petId"
        if (boundKey == key) return
        boundKey = key

        repo.observeActivities(householdId, petId)
            .onEach { list -> _state.value = _state.value.copy(activities = list) }
            .launchIn(viewModelScope)
    }
}