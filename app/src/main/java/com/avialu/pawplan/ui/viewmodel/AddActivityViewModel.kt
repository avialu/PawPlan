package com.avialu.pawplan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avialu.pawplan.data.activities.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AddActivityState(
    val type: String = "WALK",
    val note: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

class AddActivityViewModel(
    private val repo: ActivityRepository = ActivityRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(AddActivityState())
    val state: StateFlow<AddActivityState> = _state

    fun setType(v: String) = run { _state.value = _state.value.copy(type = v, error = null) }
    fun setNote(v: String) = run { _state.value = _state.value.copy(note = v, error = null) }

    fun save(householdId: String, petId: String) = viewModelScope.launch {
        val s = _state.value
        _state.value = s.copy(isLoading = true, error = null)

        try {
            repo.addActivity(householdId, petId, s.type, s.note)
            _state.value = _state.value.copy(isLoading = false, saved = true)
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Save failed")
        }
    }
}