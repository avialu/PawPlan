package com.avialu.pawplan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avialu.pawplan.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState(isLoggedIn = repo.currentUid() != null))
    val state: StateFlow<AuthUiState> = _state

    fun setEmail(v: String) = _state.value.let { _state.value = it.copy(email = v, error = null) }
    fun setPassword(v: String) = _state.value.let { _state.value = it.copy(password = v, error = null) }
    fun setDisplayName(v: String) = _state.value.let { _state.value = it.copy(displayName = v, error = null) }

    fun signIn() = viewModelScope.launch {
        val s = _state.value
        _state.value = s.copy(isLoading = true, error = null)
        try {
            repo.signIn(s.email.trim(), s.password)
            _state.value = _state.value.copy(isLoading = false, isLoggedIn = true)
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Sign in failed")
        }
    }

    fun signUp() = viewModelScope.launch {
        val s = _state.value
        _state.value = s.copy(isLoading = true, error = null)
        try {
            repo.signUp(s.email.trim(), s.password, s.displayName.trim())
            _state.value = _state.value.copy(isLoading = false, isLoggedIn = true)
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Sign up failed")
        }
    }

    fun signOut() {
        repo.signOut()
        _state.value = AuthUiState()
    }
}