package com.miseenplace.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val persons: Int? = null,
    val timeMinutes: Int? = null,
    val categories: String = "",
    val imageUri: String? = null
)
