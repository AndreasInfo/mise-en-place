package com.miseenplace.ui.list

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miseenplace.MiseEnPlace
import com.miseenplace.data.Recipe
import com.miseenplace.ui.detail.RecipeDetailActivity
import com.miseenplace.ui.edit.RecipeEditActivity
import com.miseenplace.ui.theme.MiseEnPlaceTheme

class MainActivity : ComponentActivity() {

    private val viewModel: RecipeListViewModel by viewModels {
        RecipeListViewModel.factory((application as MiseEnPlace).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Recipes"

        setContent {
            MiseEnPlaceTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(state) {
                    if (state is RecipeListUiState.Error) {
                        Toast.makeText(
                            this@MainActivity,
                            (state as RecipeListUiState.Error).message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                RecipeListScreen(
                    state = state,
                    onRecipeClick = { recipe ->
                        startActivity(Intent(this, RecipeDetailActivity::class.java).apply {
                            putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id)
                        })
                    },
                    onAddClick = {
                        startActivity(Intent(this, RecipeEditActivity::class.java))
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadRecipes()
    }
}

@Composable
private fun RecipeListScreen(
    state: RecipeListUiState,
    onRecipeClick: (Recipe) -> Unit,
    onAddClick: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Text("+")
            }
        }
    ) { padding ->
        when (state) {
            is RecipeListUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is RecipeListUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.recipes, key = { it.id }) { recipe ->
                        RecipeRow(recipe = recipe, onClick = { onRecipeClick(recipe) })
                        HorizontalDivider()
                    }
                }
            }

            is RecipeListUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Could not load recipes.")
                }
            }
        }
    }
}

@Composable
private fun RecipeRow(recipe: Recipe, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = recipe.name, fontWeight = FontWeight.Bold)
            Text(text = recipe.categories.ifEmpty { "—" })
        }
    }
}
