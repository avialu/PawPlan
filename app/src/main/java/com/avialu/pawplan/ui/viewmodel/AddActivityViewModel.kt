package com.avialu.pawplan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avialu.pawplan.data.activities.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AddActivityState(
    val selectedPetId: String = "",
    val type: String = "WALK",
    val note: String = "",
    val selectedTimestamp: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

class AddActivityViewModel(
    private val repo: ActivityRepository = ActivityRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(AddActivityState())
    val state: StateFlow<AddActivityState> = _state

    fun setSelectedPetId(v: String) {
        _state.value = _state.value.copy(selectedPetId = v, error = null)
    }

    fun setType(v: String) {
        _state.value = _state.value.copy(type = v, error = null)
    }

    fun setNote(v: String) {
        _state.value = _state.value.copy(note = v, error = null)
    }

    fun setTimestamp(v: Long?) {
        _state.value = _state.value.copy(selectedTimestamp = v, error = null)
    }

    fun initPetIfEmpty(petId: String) {
        if (_state.value.selectedPetId.isBlank()) {
            _state.value = _state.value.copy(selectedPetId = petId)
        }
    }

    fun save(householdId: String, fallbackPetId: String) = viewModelScope.launch {
        val s = _state.value
        val petId = s.selectedPetId.ifBlank { fallbackPetId }

        _state.value = s.copy(isLoading = true, error = null, saved = false)

        try {
            repo.addActivity(
                householdId = householdId,
                petId = petId,
                type = s.type,
                note = s.note,
                timestamp = s.selectedTimestamp ?: System.currentTimeMillis()
            )

            _state.value = _state.value.copy(isLoading = false, saved = true)
        } catch (e: Exception) {
            _state.value =
                _state.value.copy(isLoading = false, error = e.message ?: "Save failed")
        }
    }
}