package com.miseenplace.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.miseenplace.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeDetailViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<RecipeDetailUiState>(RecipeDetailUiState.Loading)
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    fun loadRecipe(id: Long) {
        viewModelScope.launch {
            _uiState.value = try {
                val details = repository.getRecipeWithDetails(id)
                if (details != null) RecipeDetailUiState.Success(details)
                else RecipeDetailUiState.Error("Recipe not found")
            } catch (e: Exception) {
                RecipeDetailUiState.Error(e.message ?: "Failed to load recipe")
            }
        }
    }

    fun deleteRecipe(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteRecipe(id)
                _uiState.value = RecipeDetailUiState.Deleted
            } catch (e: Exception) {
                _uiState.value = RecipeDetailUiState.Error(e.message ?: "Failed to delete recipe")
            }
        }
    }

    companion object {
        fun factory(repository: RecipeRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                RecipeDetailViewModel(repository) as T
        }
    }
}
