package com.example.myflappybird.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myflappybird.domain.repository.PlayerPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: PlayerPreferencesRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getSettings().collect { preferences -> _uiState.update { it.copy(playerName = preferences.playerName) } }
        }
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.UpdateName -> {
                _uiState.update { it.copy(playerName = event.newName) }
            }
            ProfileEvent.SaveName -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isSaving = true) }
                    repository.saveSettings(_uiState.value.playerName)
                    _uiState.update { it.copy(isSaving = false) }
                }
            }
        }
    }
}

sealed class ProfileEvent() {
    data class UpdateName(val newName: String) : ProfileEvent()
    object SaveName : ProfileEvent()
}