package com.avialu.pawplan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avialu.pawplan.data.pets.PetsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AddEditPetState(
    val name: String = "",
    val breed: String = "",
    val birthYear: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

class AddEditPetViewModel(
    private val repo: PetsRepository = PetsRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditPetState())
    val state: StateFlow<AddEditPetState> = _state

    fun setName(v: String) = run { _state.value = _state.value.copy(name = v, error = null) }
    fun setBreed(v: String) = run { _state.value = _state.value.copy(breed = v, error = null) }
    fun setBirthYear(v: String) = run {
        _state.value = _state.value.copy(birthYear = v.filter { it.isDigit() }, error = null)
    }

    fun loadForEdit(householdId: String, petId: String) = viewModelScope.launch {
        if (_state.value.isLoading) return@launch
        _state.value = _state.value.copy(isLoading = true, error = null)

        try {
            val pet = repo.getPetById(householdId, petId)
            _state.value = _state.value.copy(
                isLoading = false,
                name = pet.name,
                breed = pet.breed,
                birthYear = pet.birthYear?.toString() ?: ""
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Load failed")
        }
    }

    fun saveNew(householdId: String) = viewModelScope.launch {
        val s = _state.value
        val name = s.name.trim()
        if (name.isEmpty()) {
            _state.value = s.copy(error = "Name required")
            return@launch
        }

        _state.value = s.copy(isLoading = true, error = null)

        try {
            val by = s.birthYear.trim().toIntOrNull()
            repo.addPet(householdId, type = "dog", name = name, breed = s.breed, birthYear = by)
            _state.value = _state.value.copy(isLoading = false, saved = true)
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Save failed")
        }
    }

    fun saveEdit(householdId: String, petId: String) = viewModelScope.launch {
        val s = _state.value
        val name = s.name.trim()
        if (name.isEmpty()) {
            _state.value = s.copy(error = "Name required")
            return@launch
        }

        _state.value = s.copy(isLoading = true, error = null)

        try {
            val by = s.birthYear.trim().toIntOrNull()
            repo.updatePet(householdId, petId, type = "dog", name = name, breed = s.breed, birthYear = by)
            _state.value = _state.value.copy(isLoading = false, saved = true)
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Update failed")
        }
    }
}