package com.avialu.pawplan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avialu.pawplan.data.pets.PetsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class AddEditPetState(
    val type: String = "dog",
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

    fun setType(v: String) {
        _state.value = _state.value.copy(type = v, error = null)
    }

    fun setName(v: String) {
        _state.value = _state.value.copy(name = v, error = null)
    }

    fun setBreed(v: String) {
        _state.value = _state.value.copy(breed = v, error = null)
    }

    fun setBirthYear(v: String) {
        _state.value = _state.value.copy(
            birthYear = v.filter { it.isDigit() },
            error = null
        )
    }

    // -------------------------
    // Load for edit
    // -------------------------
    fun loadForEdit(householdId: String, petId: String) = viewModelScope.launch {

        if (_state.value.isLoading) return@launch

        _state.value = _state.value.copy(isLoading = true, error = null)

        try {
            val pet = repo.getPetById(householdId, petId)

            _state.value = _state.value.copy(
                isLoading = false,
                type = pet.type, // FIXED
                name = pet.name,
                breed = pet.breed,
                birthYear = pet.birthYear?.toString() ?: ""
            )
        } catch (e: Exception) {
            _state.value =
                _state.value.copy(isLoading = false, error = e.message ?: "Load failed")
        }
    }

    // -------------------------
    // Save new
    // -------------------------
    fun saveNew(householdId: String) = viewModelScope.launch {

        val s = _state.value
        val name = s.name.trim()

        if (name.isEmpty()) {
            _state.value = s.copy(error = "Name required")
            return@launch
        }

        // Optional: validate birth year
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val by = s.birthYear.trim().toIntOrNull()

        if (by != null && (by < 1990 || by > currentYear)) {
            _state.value = s.copy(error = "Invalid birth year")
            return@launch
        }

        _state.value = s.copy(isLoading = true, error = null)

        try {
            repo.addPet(
                householdId = householdId,
                type = s.type,   // FIXED
                name = name,
                breed = s.breed,
                birthYear = by
            )

            _state.value = _state.value.copy(
                isLoading = false,
                saved = true
            )

        } catch (e: Exception) {
            _state.value =
                _state.value.copy(isLoading = false, error = e.message ?: "Save failed")
        }
    }

    // -------------------------
    // Save edit
    // -------------------------
    fun saveEdit(householdId: String, petId: String) = viewModelScope.launch {

        val s = _state.value
        val name = s.name.trim()

        if (name.isEmpty()) {
            _state.value = s.copy(error = "Name required")
            return@launch
        }

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val by = s.birthYear.trim().toIntOrNull()

        if (by != null && (by < 1990 || by > currentYear)) {
            _state.value = s.copy(error = "Invalid birth year")
            return@launch
        }

        _state.value = s.copy(isLoading = true, error = null)

        try {
            repo.updatePet(
                householdId = householdId,
                petId = petId,
                type = s.type, // FIXED
                name = name,
                breed = s.breed,
                birthYear = by
            )

            _state.value = _state.value.copy(
                isLoading = false,
                saved = true
            )

        } catch (e: Exception) {
            _state.value =
                _state.value.copy(isLoading = false, error = e.message ?: "Update failed")
        }
    }
}