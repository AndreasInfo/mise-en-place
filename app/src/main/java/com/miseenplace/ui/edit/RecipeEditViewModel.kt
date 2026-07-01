package com.miseenplace.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.miseenplace.data.Ingredient
import com.miseenplace.data.Recipe
import com.miseenplace.data.Step
import com.miseenplace.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeEditViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<RecipeEditUiState>(RecipeEditUiState.Idle)
    val uiState: StateFlow<RecipeEditUiState> = _uiState.asStateFlow()

    fun loadRecipe(id: Long) {
        viewModelScope.launch {
            _uiState.value = try {
                val details = repository.getRecipeWithDetails(id)
                if (details != null) RecipeEditUiState.Loaded(details)
                else RecipeEditUiState.Error("Recipe not found")
            } catch (e: Exception) {
                RecipeEditUiState.Error(e.message ?: "Failed to load recipe")
            }
        }
    }

    fun save(recipe: Recipe, ingredients: List<Ingredient>, steps: List<Step>) {
        viewModelScope.launch {
            _uiState.value = try {
                repository.saveRecipe(recipe, ingredients, steps)
                RecipeEditUiState.Saved
            } catch (e: Exception) {
                RecipeEditUiState.Error(e.message ?: "Failed to save recipe")
            }
        }
    }

    companion object {
        fun factory(repository: RecipeRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                RecipeEditViewModel(repository) as T
        }
    }
}
