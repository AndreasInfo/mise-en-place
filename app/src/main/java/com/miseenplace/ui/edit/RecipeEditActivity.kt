package com.miseenplace.ui.edit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miseenplace.MiseEnPlace
import com.miseenplace.data.Ingredient
import com.miseenplace.data.Recipe
import com.miseenplace.data.Step
import com.miseenplace.ui.theme.MiseEnPlaceTheme

class RecipeEditActivity : ComponentActivity() {

    companion object {
        const val EXTRA_RECIPE_ID = "recipe_id"
    }

    private val viewModel: RecipeEditViewModel by viewModels {
        RecipeEditViewModel.factory((application as MiseEnPlace).repository)
    }

    private var recipeId: Long = -1
    private var selectedImageUri by mutableStateOf<String?>(null)

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            selectedImageUri = uri.toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        recipeId = intent.getLongExtra(EXTRA_RECIPE_ID, -1)
        title = if (recipeId == -1L) "New Recipe" else "Edit Recipe"

        if (recipeId != -1L) {
            viewModel.loadRecipe(recipeId)
        }

        setContent {
            MiseEnPlaceTheme {
                RecipeEditScreen(
                    uiState = viewModel.uiState.collectAsStateWithLifecycle().value,
                    selectedImageUri = selectedImageUri,
                    onPickImage = { imagePicker.launch("image/*") },
                    onSave = { recipe, ingredients, steps -> viewModel.save(recipe, ingredients, steps) },
                    onSaved = { finish() },
                    onError = { message ->
                        Toast.makeText(this@RecipeEditActivity, message, Toast.LENGTH_SHORT).show()
                    },
                    recipeId = recipeId,
                    onImageUriChanged = { selectedImageUri = it }
                )
            }
        }
    }
}

private data class IngredientInput(
    val name: String = "",
    val amount: String = "",
    val unit: String = ""
)

private data class StepInput(
    val description: String = ""
)

private enum class Category(val label: String) {
    FISH("Fish"),
    MEAT("Meat"),
    BREAKFAST("Breakfast"),
    POTATOES("Potatoes"),
    CAKE("Cake"),
    PASTA("Pasta"),
    SALAD("Salad"),
    OTHER("Other"),
    SOUP("Soup"),
    SUSHI("Sushi"),
    SWEETS("Sweets"),
    VEGAN("Vegan"),
    VEGETARIAN("Vegetarian"),
    CHRISTMAS("Christmas"),
}

private fun parseCategories(raw: String): Set<Category> =
    raw.split(',')
        .map { it.trim() }
        .mapNotNull { value ->
            Category.values().firstOrNull { category ->
                category.name == value || category.label.equals(value, ignoreCase = true)
            }
        }
        .toSet()

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipeEditScreen(
    uiState: RecipeEditUiState,
    selectedImageUri: String?,
    recipeId: Long,
    onPickImage: () -> Unit,
    onSave: (Recipe, List<Ingredient>, List<Step>) -> Unit,
    onSaved: () -> Unit,
    onError: (String) -> Unit,
    onImageUriChanged: (String?) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var persons by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }
    var categories by rememberSaveable { mutableStateOf(setOf<Category>()) }
    var nameError by rememberSaveable { mutableStateOf(false) }

    val ingredients = remember { mutableStateListOf<IngredientInput>() }
    val steps = remember { mutableStateListOf<StepInput>() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is RecipeEditUiState.Loaded -> {
                val recipe = uiState.details.recipe
                name = recipe.name
                persons = recipe.persons?.toString() ?: ""
                time = recipe.timeMinutes?.toString() ?: ""
                categories = parseCategories(recipe.categories)
                onImageUriChanged(recipe.imageUri)
                ingredients.clear()
                ingredients.addAll(
                    uiState.details.ingredients.map { ing ->
                        IngredientInput(name = ing.name, amount = ing.amount, unit = ing.unit)
                    }
                )
                steps.clear()
                steps.addAll(
                    uiState.details.steps.sortedBy { it.orderIndex }.map { step ->
                        StepInput(description = step.description)
                    }
                )
            }

            is RecipeEditUiState.Saved -> onSaved()
            is RecipeEditUiState.Error -> onError(uiState.message)
            is RecipeEditUiState.Idle -> Unit
        }
    }

    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = {
                name = it
                if (it.isNotBlank()) nameError = false
            },
            label = { Text("Name") },
            isError = nameError,
            supportingText = {
                if (nameError) {
                    Text("Name is required")
                }
            }
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = persons,
            onValueChange = { persons = it },
            label = { Text("Persons") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = time,
            onValueChange = { time = it },
            label = { Text("Time (minutes)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Text("Categories", style = MaterialTheme.typography.titleMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Category.entries.forEach { category ->
                FilterChip(
                    selected = categories.contains(category),
                    onClick = {
                        categories = if (categories.contains(category)) {
                            categories - category
                        } else {
                            categories + category
                        }
                    },
                    label = { Text(category.label) }
                )
            }
        }

        Button(onClick = onPickImage) { Text("Pick image") }

        if (selectedImageUri != null) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                factory = { context -> ImageView(context) },
                update = { imageView -> imageView.setImageURI(Uri.parse(selectedImageUri)) }
            )
        }

        Text("Ingredients", style = MaterialTheme.typography.titleMedium)
        ingredients.forEachIndexed { index, ingredient ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = ingredient.name,
                    onValueChange = { value ->
                        ingredients[index] = ingredient.copy(name = value)
                    },
                    label = { Text("Name") }
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = ingredient.amount,
                    onValueChange = { value ->
                        ingredients[index] = ingredient.copy(amount = value)
                    },
                    label = { Text("Amount") }
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = ingredient.unit,
                    onValueChange = { value ->
                        ingredients[index] = ingredient.copy(unit = value)
                    },
                    label = { Text("Unit") }
                )
            }
            Button(onClick = { ingredients.removeAt(index) }) { Text("Remove ingredient") }
        }
        Button(onClick = { ingredients.add(IngredientInput()) }) { Text("Add ingredient") }

        Text("Steps", style = MaterialTheme.typography.titleMedium)
        steps.forEachIndexed { index, step ->
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = step.description,
                onValueChange = { value ->
                    steps[index] = step.copy(description = value)
                },
                label = { Text("Step ${index + 1}") }
            )
            Button(onClick = { steps.removeAt(index) }) { Text("Remove step") }
        }
        Button(onClick = { steps.add(StepInput()) }) { Text("Add step") }

        Button(
            onClick = {
                if (name.trim().isEmpty()) {
                    nameError = true
                    return@Button
                }

                val targetId = if (recipeId == -1L) 0L else recipeId
                val recipe = Recipe(
                    id = targetId,
                    name = name.trim(),
                    persons = persons.trim().toIntOrNull(),
                    timeMinutes = time.trim().toIntOrNull(),
                    categories = categories.joinToString(", ") { it.name },
                    imageUri = selectedImageUri
                )
                val recipeIngredients = ingredients.mapNotNull { ing ->
                    val ingredientName = ing.name.trim()
                    if (ingredientName.isEmpty()) null
                    else Ingredient(
                        recipeId = targetId,
                        name = ingredientName,
                        amount = ing.amount.trim(),
                        unit = ing.unit.trim()
                    )
                }
                val recipeSteps = steps.mapIndexedNotNull { index, step ->
                    val description = step.description.trim()
                    if (description.isEmpty()) null
                    else Step(
                        recipeId = targetId,
                        orderIndex = index,
                        description = description
                    )
                }
                onSave(recipe, recipeIngredients, recipeSteps)
            }
        ) {
            Text("Save")
        }
    }
}
