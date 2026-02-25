package com.avialu.pawplan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avialu.pawplan.data.models.Pet
import com.avialu.pawplan.data.pets.PetsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class HomeDashboardState(
    val pets: List<Pet> = emptyList(),
    val error: String? = null
)

class HomeDashboardViewModel(
    private val repo: PetsRepository = PetsRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(HomeDashboardState())
    val state: StateFlow<HomeDashboardState> = _state

    private var boundHouseholdId: String? = null

    fun bind(householdId: String) {
        if (boundHouseholdId == householdId) return
        boundHouseholdId = householdId

        repo.observePets(householdId)
            .onEach { list -> _state.value = _state.value.copy(pets = list) }
            .launchIn(viewModelScope)
    }
}