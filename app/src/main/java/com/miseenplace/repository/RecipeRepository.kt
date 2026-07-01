package com.miseenplace.repository

import com.miseenplace.data.Ingredient
import com.miseenplace.data.Recipe
import com.miseenplace.data.RecipeWithDetails
import com.miseenplace.data.Step

interface RecipeRepository {
    suspend fun getAllRecipes(): List<Recipe>
    suspend fun getRecipeWithDetails(id: Long): RecipeWithDetails?
    suspend fun saveRecipe(recipe: Recipe, ingredients: List<Ingredient>, steps: List<Step>): Long
    suspend fun deleteRecipe(id: Long)
}
