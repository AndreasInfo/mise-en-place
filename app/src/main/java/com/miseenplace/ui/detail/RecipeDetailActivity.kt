package com.miseenplace.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miseenplace.MiseEnPlace
import com.miseenplace.data.RecipeWithDetails
import com.miseenplace.ui.edit.RecipeEditActivity
import com.miseenplace.ui.theme.MiseEnPlaceTheme

class RecipeDetailActivity : ComponentActivity() {

    companion object {
        const val EXTRA_RECIPE_ID = "recipe_id"
    }

    private val viewModel: RecipeDetailViewModel by viewModels {
        RecipeDetailViewModel.factory((application as MiseEnPlace).repository)
    }

    private var recipeId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        recipeId = intent.getLongExtra(EXTRA_RECIPE_ID, -1)

        setContent {
            MiseEnPlaceTheme {
                var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(state) {
                    when (state) {
                        is RecipeDetailUiState.Success -> {
                            title = (state as RecipeDetailUiState.Success).details.recipe.name
                        }

                        is RecipeDetailUiState.Deleted -> finish()
                        is RecipeDetailUiState.Error -> {
                            Toast.makeText(
                                this@RecipeDetailActivity,
                                (state as RecipeDetailUiState.Error).message,
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }

                        is RecipeDetailUiState.Loading -> Unit
                    }
                }

                RecipeDetailScreen(
                    state = state,
                    onEdit = {
                        startActivity(Intent(this, RecipeEditActivity::class.java).apply {
                            putExtra(RecipeEditActivity.EXTRA_RECIPE_ID, recipeId)
                        })
                    },
                    onDelete = { showDeleteDialog = true }
                )

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Delete Recipe") },
                        text = { Text("Are you sure you want to delete this recipe?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteDialog = false
                                    viewModel.deleteRecipe(recipeId)
                                }
                            ) { Text("Delete") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadRecipe(recipeId)
    }
}

@Composable
private fun RecipeDetailScreen(
    state: RecipeDetailUiState,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    when (state) {
        is RecipeDetailUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is RecipeDetailUiState.Success -> {
            RecipeDetailContent(
                details = state.details,
                onEdit = onEdit,
                onDelete = onDelete
            )
        }

        is RecipeDetailUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Could not load recipe.")
            }
        }

        is RecipeDetailUiState.Deleted -> Unit
    }
}

@Composable
private fun RecipeDetailContent(
    details: RecipeWithDetails,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val recipe = details.recipe
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onEdit) { Text("Edit") }
            Button(onClick = onDelete) { Text("Delete") }
        }

        if (recipe.imageUri != null) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                factory = { context -> ImageView(context) },
                update = { imageView -> imageView.setImageURI(Uri.parse(recipe.imageUri)) }
            )
        }

        LabelValueRow(label = "Name", value = recipe.name)
        LabelValueRow(label = "Persons", value = recipe.persons?.toString() ?: "—")
        LabelValueRow(
            label = "Time",
            value = if (recipe.timeMinutes != null) "${recipe.timeMinutes} min" else "—"
        )
        LabelValueRow(label = "Categories", value = recipe.categories.ifEmpty { "—" })

        Text("Ingredients", style = MaterialTheme.typography.titleMedium)
        details.ingredients.forEach { ing ->
            val suffix = buildString {
                if (ing.amount.isNotEmpty()) append(ing.amount)
                if (ing.unit.isNotEmpty()) append(" ${ing.unit}")
            }
            val line = if (suffix.isNotEmpty()) "• ${ing.name}  ($suffix)" else "• ${ing.name}"
            Text(line)
        }

        Text("Steps", style = MaterialTheme.typography.titleMedium)
        details.steps.sortedBy { it.orderIndex }.forEachIndexed { index, step ->
            Text("${index + 1}.  ${step.description}")
        }
    }
}

@Composable
private fun LabelValueRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value)
    }
}
