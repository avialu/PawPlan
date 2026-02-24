package com.avialu.pawplan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avialu.pawplan.data.household.HouseholdRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

data class OnboardingState(
    val householdName: String = "",
    val joinCode: String = "",
    val createdJoinCode: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class OnboardingViewModel(
    private val repo: HouseholdRepository = HouseholdRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state

    fun setHouseholdName(v: String) {
        _state.value = _state.value.copy(householdName = v, error = null)
    }

    fun setJoinCode(v: String) {
        _state.value = _state.value.copy(joinCode = v, error = null)
    }

    fun createHousehold() = viewModelScope.launch {
        val name = _state.value.householdName.trim()
        if (name.isEmpty()) {
            _state.value = _state.value.copy(error = "Household name required")
            return@launch
        }

        _state.value = _state.value.copy(isLoading = true, error = null)

        try {
            val code = withTimeout(15_000) { // כדי שלא ניתקע לנצח
                repo.createHousehold(name)
            }
            _state.value = _state.value.copy(
                isLoading = false,
                createdJoinCode = code
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = e.message ?: "Create failed"
            )
        }
    }

    fun joinHousehold() = viewModelScope.launch {
        val code = _state.value.joinCode.trim()
        if (code.length < 4) {
            _state.value = _state.value.copy(error = "Join code too short")
            return@launch
        }

        _state.value = _state.value.copy(isLoading = true, error = null)

        try {
            withTimeout(15_000) {
                repo.joinHouseholdByCode(code)
            }
            // אם הצטרף בהצלחה אין מה להציג, ננווט ל-Home מה-UI
            _state.value = _state.value.copy(isLoading = false)
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = e.message ?: "Join failed"
            )
        }
    }

    fun clearCreatedCode() {
        _state.value = _state.value.copy(createdJoinCode = null)
    }
}