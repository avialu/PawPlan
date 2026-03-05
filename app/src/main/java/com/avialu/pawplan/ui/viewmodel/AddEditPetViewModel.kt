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

    // settings
    val feedsPerDay: String = "2",           // 1..6
    val walksPerDay: String = "2",           // 1..6 (dog only)

    val vaccinationEnabled: Boolean = true,
    val vaccinationEveryMonths: String = "3",

    val groomingEnabled: Boolean = true,
    val groomingEveryMonths: String = "4",

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
    fun setName(v: String) = run { _state.value = _state.value.copy(name = v, error = null) }
    fun setBreed(v: String) = run { _state.value = _state.value.copy(breed = v, error = null) }
    fun setBirthYear(v: String) = run { _state.value = _state.value.copy(birthYear = v.filter { it.isDigit() }, error = null) }

    fun setFeedsPerDay(v: String) = run { _state.value = _state.value.copy(feedsPerDay = v.filter { it.isDigit() }, error = null) }
    fun setWalksPerDay(v: String) = run { _state.value = _state.value.copy(walksPerDay = v.filter { it.isDigit() }, error = null) }

    fun setVaccinationEnabled(v: Boolean) = run { _state.value = _state.value.copy(vaccinationEnabled = v, error = null) }
    fun setVaccinationEveryMonths(v: String) = run { _state.value = _state.value.copy(vaccinationEveryMonths = v.filter { it.isDigit() }, error = null) }

    fun setGroomingEnabled(v: Boolean) = run { _state.value = _state.value.copy(groomingEnabled = v, error = null) }
    fun setGroomingEveryMonths(v: String) = run { _state.value = _state.value.copy(groomingEveryMonths = v.filter { it.isDigit() }, error = null) }

    fun loadForEdit(householdId: String, petId: String) = viewModelScope.launch {
        if (_state.value.isLoading) return@launch
        _state.value = _state.value.copy(isLoading = true, error = null)

        try {
            val pet = repo.getPetById(householdId, petId)
            _state.value = _state.value.copy(
                isLoading = false,

                type = pet.type,
                name = pet.name,
                breed = pet.breed,
                birthYear = pet.birthYear?.toString() ?: "",

                feedsPerDay = pet.feedsPerDay.toString(),
                walksPerDay = pet.walksPerDay.toString(),

                vaccinationEnabled = pet.vaccinationEnabled,
                vaccinationEveryMonths = pet.vaccinationEveryMonths.toString(),

                groomingEnabled = pet.groomingEnabled,
                groomingEveryMonths = pet.groomingEveryMonths.toString()
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Load failed")
        }
    }

    private fun validate(): String? {
        val s = _state.value
        if (s.name.trim().isEmpty()) return "Name is required"

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val by = s.birthYear.trim().toIntOrNull()
        if (by != null && (by < 1990 || by > currentYear)) return "Invalid birth year"

        val feeds = s.feedsPerDay.toIntOrNull() ?: return "Feeds per day must be a number"
        if (feeds !in 1..6) return "Feeds per day must be between 1 and 6"

        val isDog = s.type.lowercase() == "dog"
        val walks = s.walksPerDay.toIntOrNull() ?: 0
        if (isDog && walks !in 1..6) return "Walks per day must be between 1 and 6"

        val vaccMonths = s.vaccinationEveryMonths.toIntOrNull() ?: return "Vaccination interval must be a number"
        if (vaccMonths < 1) return "Vaccination interval must be at least 1 month"

        val groomMonths = s.groomingEveryMonths.toIntOrNull() ?: return "Grooming interval must be a number"
        if (groomMonths < 1) return "Grooming interval must be at least 1 month"

        return null
    }

    fun saveNew(householdId: String) = viewModelScope.launch {
        val err = validate()
        if (err != null) {
            _state.value = _state.value.copy(error = err)
            return@launch
        }

        val s = _state.value
        _state.value = s.copy(isLoading = true, error = null)

        try {
            val by = s.birthYear.trim().toIntOrNull()
            val feeds = s.feedsPerDay.toInt()
            val walks = if (s.type.lowercase() == "dog") s.walksPerDay.toInt() else 0

            repo.addPet(
                householdId = householdId,
                type = s.type,
                name = s.name.trim(),
                breed = s.breed,
                birthYear = by,
                feedsPerDay = feeds,
                walksPerDay = walks,
                vaccinationEnabled = s.vaccinationEnabled,
                vaccinationEveryMonths = s.vaccinationEveryMonths.toInt(),
                groomingEnabled = s.groomingEnabled,
                groomingEveryMonths = s.groomingEveryMonths.toInt()
            )

            _state.value = _state.value.copy(isLoading = false, saved = true)
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Save failed")
        }
    }

    fun saveEdit(householdId: String, petId: String) = viewModelScope.launch {
        val err = validate()
        if (err != null) {
            _state.value = _state.value.copy(error = err)
            return@launch
        }

        val s = _state.value
        _state.value = s.copy(isLoading = true, error = null)

        try {
            val by = s.birthYear.trim().toIntOrNull()
            val feeds = s.feedsPerDay.toInt()
            val walks = if (s.type.lowercase() == "dog") s.walksPerDay.toInt() else 0

            repo.updatePet(
                householdId = householdId,
                petId = petId,
                type = s.type,
                name = s.name.trim(),
                breed = s.breed,
                birthYear = by,
                feedsPerDay = feeds,
                walksPerDay = walks,
                vaccinationEnabled = s.vaccinationEnabled,
                vaccinationEveryMonths = s.vaccinationEveryMonths.toInt(),
                groomingEnabled = s.groomingEnabled,
                groomingEveryMonths = s.groomingEveryMonths.toInt()
            )

            _state.value = _state.value.copy(isLoading = false, saved = true)
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Update failed")
        }
    }
}