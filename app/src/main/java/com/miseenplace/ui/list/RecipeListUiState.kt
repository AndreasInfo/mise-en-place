package com.miseenplace.ui.list

import com.miseenplace.data.Recipe

sealed class RecipeListUiState {
    object Loading : RecipeListUiState()
    data class Success(val recipes: List<Recipe>) : RecipeListUiState()
    data class Error(val message: String) : RecipeListUiState()
}
