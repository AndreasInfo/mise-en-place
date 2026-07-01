package com.miseenplace.data

import androidx.room.*

@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipes ORDER BY name ASC")
    suspend fun getAllRecipes(): List<Recipe>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeWithDetails(id: Long): RecipeWithDetails?

    @Insert
    suspend fun insertRecipe(recipe: Recipe): Long

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipeById(id: Long)

    @Insert
    suspend fun insertIngredients(ingredients: List<Ingredient>)

    @Insert
    suspend fun insertSteps(steps: List<Step>)

    @Query("DELETE FROM ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsForRecipe(recipeId: Long)

    @Query("DELETE FROM steps WHERE recipeId = :recipeId")
    suspend fun deleteStepsForRecipe(recipeId: Long)
}
