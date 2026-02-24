package com.avialu.pawplan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avialu.pawplan.data.feed.FeedRepository
import com.avialu.pawplan.data.models.FeedEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class HomeState(
    val feed: List<FeedEvent> = emptyList(),
    val error: String? = null
)

class HomeViewModel(
    private val repo: FeedRepository = FeedRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    private var boundHouseholdId: String? = null

    fun bind(householdId: String) {
        if (boundHouseholdId == householdId) return
        boundHouseholdId = householdId

        repo.observeFeed(householdId)
            .onEach { list -> _state.value = _state.value.copy(feed = list) }
            .launchIn(viewModelScope)
    }
}