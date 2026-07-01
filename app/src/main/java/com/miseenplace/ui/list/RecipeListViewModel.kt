package com.miseenplace.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.miseenplace.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeListViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<RecipeListUiState>(RecipeListUiState.Loading)
    val uiState: StateFlow<RecipeListUiState> = _uiState.asStateFlow()

    fun loadRecipes() {
        viewModelScope.launch {
            _uiState.value = try {
                RecipeListUiState.Success(repository.getAllRecipes())
            } catch (e: Exception) {
                RecipeListUiState.Error(e.message ?: "Failed to load recipes")
            }
        }
    }

    companion object {
        fun factory(repository: RecipeRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                RecipeListViewModel(repository) as T
        }
    }
}
