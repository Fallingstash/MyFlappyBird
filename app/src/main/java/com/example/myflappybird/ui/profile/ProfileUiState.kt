package com.example.myflappybird.ui.profile

data class ProfileUiState(
    val playerName: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false
)
