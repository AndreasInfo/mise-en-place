package com.miseenplace.repository

import com.miseenplace.data.Ingredient
import com.miseenplace.data.Recipe
import com.miseenplace.data.RecipeDao
import com.miseenplace.data.RecipeWithDetails
import com.miseenplace.data.Step

class RecipeRepositoryImpl(private val dao: RecipeDao) : RecipeRepository {

    override suspend fun getAllRecipes(): List<Recipe> =
        dao.getAllRecipes()

    override suspend fun getRecipeWithDetails(id: Long): RecipeWithDetails? =
        dao.getRecipeWithDetails(id)

    override suspend fun saveRecipe(
        recipe: Recipe,
        ingredients: List<Ingredient>,
        steps: List<Step>
    ): Long {
        val id = if (recipe.id == 0L) {
            dao.insertRecipe(recipe)
        } else {
            dao.updateRecipe(recipe)
            dao.deleteIngredientsForRecipe(recipe.id)
            dao.deleteStepsForRecipe(recipe.id)
            recipe.id
        }
        dao.insertIngredients(ingredients.map { it.copy(recipeId = id) })
        dao.insertSteps(steps.map { it.copy(recipeId = id) })
        return id
    }

    override suspend fun deleteRecipe(id: Long) =
        dao.deleteRecipeById(id)
}
