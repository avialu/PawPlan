package com.avialu.pawplan.ui.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avialu.pawplan.data.activities.ActivityRepository
import com.avialu.pawplan.data.household.HouseholdRepository
import com.avialu.pawplan.data.models.Household
import com.avialu.pawplan.data.models.PetActivity
import com.avialu.pawplan.data.models.User
import com.avialu.pawplan.data.storage.StorageRepository
import com.avialu.pawplan.data.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class ProfilePhotoState(
    val isUploading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(
    private val repository: UserRepository = UserRepository(),
    private val storageRepo: StorageRepository = StorageRepository(),
    private val activityRepo: ActivityRepository = ActivityRepository(),
    private val householdRepo: HouseholdRepository = HouseholdRepository()
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _userActivities = MutableStateFlow<List<PetActivity>>(emptyList())
    val userActivities: StateFlow<List<PetActivity>> = _userActivities

    private val _ownerName = MutableStateFlow<String?>(null)
    val ownerName: StateFlow<String?> = _ownerName

    private val _household = MutableStateFlow<Household?>(null)
    val household: StateFlow<Household?> = _household

    private val _photoState = MutableStateFlow(ProfilePhotoState())
    val photoState: StateFlow<ProfilePhotoState> = _photoState

    private var observedHouseholdId: String? = null
    private var observedActivitiesKey: String? = null
    private var observedOwnerUid: String? = null

    init {
        repository.observeUserProfile()
            .onEach { currentUser ->
                _user.value = currentUser

                val householdId = currentUser?.activeHouseholdId
                val uid = currentUser?.uid

                if (householdId.isNullOrBlank() || uid.isNullOrBlank()) {
                    _household.value = null
                    _ownerName.value = null
                    _userActivities.value = emptyList()
                    observedHouseholdId = null
                    observedActivitiesKey = null
                    observedOwnerUid = null
                    return@onEach
                }

                if (observedHouseholdId != householdId) {
                    observedHouseholdId = householdId

                    householdRepo.observeHousehold(householdId)
                        .onEach { household ->
                            _household.value = household

                            val ownerUid = household?.createdBy
                            if (!ownerUid.isNullOrBlank() && observedOwnerUid != ownerUid) {
                                observedOwnerUid = ownerUid

                                householdRepo.observeUserDisplayName(ownerUid)
                                    .onEach { _ownerName.value = it }
                                    .launchIn(viewModelScope)
                            }
                        }
                        .launchIn(viewModelScope)
                }

                val activitiesKey = "$householdId|$uid"
                if (observedActivitiesKey != activitiesKey) {
                    observedActivitiesKey = activitiesKey

                    activityRepo.observeUserActivities(householdId, uid)
                        .onEach { _userActivities.value = it }
                        .launchIn(viewModelScope)
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