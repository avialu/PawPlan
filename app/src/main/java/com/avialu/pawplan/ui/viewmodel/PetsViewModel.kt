package com.avialu.pawplan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avialu.pawplan.data.models.Pet
import com.avialu.pawplan.data.pets.PetsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class PetsState(
    val pets: List<Pet> = emptyList(),
    val name: String = "",
    val breed: String = "",
    val birthYear: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class PetsViewModel(
    private val repo: PetsRepository = PetsRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(PetsState())
    val state: StateFlow<PetsState> = _state

    private var bound = false

    fun bind(householdId: String) {
        if (bound) return
        bound = true

        repo.observePets(householdId)
            .onEach { list -> _state.value = _state.value.copy(pets = list) }
            .launchIn(viewModelScope)
    }

    fun setName(v: String) = run { _state.value = _state.value.copy(name = v, error = null) }
    fun setBreed(v: String) = run { _state.value = _state.value.copy(breed = v, error = null) }
    fun setBirthYear(v: String) = run { _state.value = _state.value.copy(birthYear = v.filter { it.isDigit() }, error = null) }

    fun addPet(householdId: String) = viewModelScope.launch {
        val s = _state.value
        val name = s.name.trim()
        if (name.isEmpty()) {
            _state.value = s.copy(error = "Name required")
            return@launch
        }

        _state.value = s.copy(isLoading = true, error = null)

        try {
            val by = s.birthYear.trim().toIntOrNull()
            repo.addPet(
                householdId = householdId,
                type = "dog", // כרגע קבוע, בהמשך נעשה בחירה
                name = name,
                breed = s.breed,
                birthYear = by
            )
            _state.value = _state.value.copy(
                isLoading = false,
                name = "",
                breed = "",
                birthYear = ""
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Add failed")
        }
    }

    fun deletePet(householdId: String, petId: String) = viewModelScope.launch {
        try {
            repo.deletePet(householdId, petId)
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = e.message ?: "Delete failed")
        }
    }
}