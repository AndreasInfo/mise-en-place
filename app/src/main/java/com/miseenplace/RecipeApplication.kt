package com.miseenplace

import android.app.Application
import com.miseenplace.data.AppDatabase
import com.miseenplace.repository.RecipeRepository
import com.miseenplace.repository.RecipeRepositoryImpl

class RecipeApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val repository: RecipeRepository by lazy { RecipeRepositoryImpl(database.recipeDao()) }
}
