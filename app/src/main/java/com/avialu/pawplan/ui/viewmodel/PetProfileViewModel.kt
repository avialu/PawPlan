package com.avialu.pawplan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avialu.pawplan.data.models.Pet
import com.avialu.pawplan.data.pets.PetsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PetProfileState(
    val pet: Pet? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val deleted: Boolean = false
)

class PetProfileViewModel(
    private val repo: PetsRepository = PetsRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(PetProfileState())
    val state: StateFlow<PetProfileState> = _state

    fun load(householdId: String, petId: String) = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true, error = null)
        try {
            val pet = repo.getPetById(householdId, petId)
            _state.value = _state.value.copy(isLoading = false, pet = pet)
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Load failed")
        }
    }

    fun delete(householdId: String, petId: String) = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true, error = null)
        try {
            repo.deletePet(householdId, petId)
            _state.value = _state.value.copy(isLoading = false, deleted = true)
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Delete failed")
        }
    }
}