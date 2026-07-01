package com.miseenplace.ui.detail

import com.miseenplace.data.RecipeWithDetails

sealed class RecipeDetailUiState {
    object Loading : RecipeDetailUiState()
    data class Success(val details: RecipeWithDetails) : RecipeDetailUiState()
    object Deleted : RecipeDetailUiState()
    data class Error(val message: String) : RecipeDetailUiState()
}
