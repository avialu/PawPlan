package com.avialu.pawplan.ui.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avialu.pawplan.data.household.HouseholdRepository
import com.avialu.pawplan.data.models.Household
import com.avialu.pawplan.data.models.User
import com.avialu.pawplan.data.storage.StorageRepository
import com.avialu.pawplan.data.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch

data class ProfilePhotoState(
    val isUploading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(
    private val repository: UserRepository = UserRepository(),
    private val storageRepo: StorageRepository = StorageRepository()
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val householdRepo = HouseholdRepository()

    private val _household = MutableStateFlow<Household?>(null)
    val household: StateFlow<Household?> = _household
    private val _photoState = MutableStateFlow(ProfilePhotoState())
    val photoState: StateFlow<ProfilePhotoState> = _photoState

    init {
        repository.observeUserProfile()
            .onEach { user ->
                _user.value = user

                val householdId = user?.activeHouseholdId
                if (!householdId.isNullOrBlank()) {
                    householdRepo.observeHousehold(householdId)
                        .onEach { _household.value = it }
                        .launchIn(viewModelScope)
                } else {
                    _household.value = null
                }
            }
            .launchIn(viewModelScope)
    }

    fun leaveHousehold() = viewModelScope.launch {
        repository.updateActiveHousehold(null)
    }

    fun uploadProfilePhoto(uri: Uri, contentResolver: ContentResolver) = viewModelScope.launch {
        val uid = _user.value?.uid
        if (uid.isNullOrBlank()) {
            _photoState.value = ProfilePhotoState(error = "No user")
            return@launch
        }

        _photoState.value = ProfilePhotoState(isUploading = true)

        try {
            val url = storageRepo.uploadUserProfileImage(uid, uri, contentResolver)
            repository.updatePhotoUrl(url)
            _photoState.value = ProfilePhotoState(isUploading = false)
        } catch (e: Exception) {
            _photoState.value = ProfilePhotoState(
                isUploading = false,
                error = e.message ?: "Upload failed"
            )
        }
    }
}