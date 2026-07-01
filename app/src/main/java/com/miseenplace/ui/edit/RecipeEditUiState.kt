package com.miseenplace.ui.edit

import com.miseenplace.data.RecipeWithDetails

sealed class RecipeEditUiState {
    object Idle : RecipeEditUiState()
    data class Loaded(val details: RecipeWithDetails) : RecipeEditUiState()
    object Saved : RecipeEditUiState()
    data class Error(val message: String) : RecipeEditUiState()
}
