package com.miseenplace.ui.edit

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.miseenplace.R
import com.miseenplace.RecipeApplication
import com.miseenplace.data.Ingredient
import com.miseenplace.data.Recipe
import com.miseenplace.data.Step
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecipeEditActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_RECIPE_ID = "recipe_id"
    }

    private val viewModel: RecipeEditViewModel by viewModels {
        RecipeEditViewModel.factory((application as RecipeApplication).repository)
    }

    private var recipeId: Long = -1
    private var selectedImageUri: String? = null

    private lateinit var etName: EditText
    private lateinit var etPersons: EditText
    private lateinit var etTime: EditText
    private lateinit var etCategories: EditText
    private lateinit var imgPreview: ImageView
    private lateinit var llIngredients: LinearLayout
    private lateinit var llSteps: LinearLayout

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            selectedImageUri = uri.toString()
            imgPreview.visibility = View.VISIBLE
            imgPreview.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_edit)

        recipeId = intent.getLongExtra(EXTRA_RECIPE_ID, -1)
        title = if (recipeId == -1L) "New Recipe" else "Edit Recipe"

        etName = findViewById(R.id.etName)
        etPersons = findViewById(R.id.etPersons)
        etTime = findViewById(R.id.etTime)
        etCategories = findViewById(R.id.etCategories)
        imgPreview = findViewById(R.id.imgPreview)
        llIngredients = findViewById(R.id.llIngredients)
        llSteps = findViewById(R.id.llSteps)

        findViewById<Button>(R.id.btnPickImage).setOnClickListener { imagePicker.launch("image/*") }
        findViewById<Button>(R.id.btnAddIngredient).setOnClickListener { addIngredientRow() }
        findViewById<Button>(R.id.btnAddStep).setOnClickListener { addStepRow() }
        findViewById<Button>(R.id.btnSave).setOnClickListener { save() }

        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is RecipeEditUiState.Idle -> Unit
                    is RecipeEditUiState.Loaded -> {
                        val r = state.details.recipe
                        etName.setText(r.name)
                        etPersons.setText(r.persons?.toString() ?: "")
                        etTime.setText(r.timeMinutes?.toString() ?: "")
                        etCategories.setText(r.categories)
                        if (r.imageUri != null) {
                            selectedImageUri = r.imageUri
                            imgPreview.visibility = View.VISIBLE
                            imgPreview.setImageURI(Uri.parse(r.imageUri))
                        }
                        llIngredients.removeAllViews()
                        state.details.ingredients.forEach { addIngredientRow(it.name, it.amount, it.unit) }
                        llSteps.removeAllViews()
                        state.details.steps.sortedBy { it.orderIndex }.forEach { addStepRow(it.description) }
                    }
                    is RecipeEditUiState.Saved -> finish()
                    is RecipeEditUiState.Error -> Toast.makeText(this@RecipeEditActivity, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        if (recipeId != -1L) viewModel.loadRecipe(recipeId)
    }

    private fun addIngredientRow(name: String = "", amount: String = "", unit: String = "") {
        val row = LayoutInflater.from(this).inflate(R.layout.item_ingredient_row, llIngredients, false)
        row.findViewById<EditText>(R.id.etIngName).setText(name)
        row.findViewById<EditText>(R.id.etIngAmount).setText(amount)
        row.findViewById<EditText>(R.id.etIngUnit).setText(unit)
        row.findViewById<Button>(R.id.btnRemoveIng).setOnClickListener { llIngredients.removeView(row) }
        llIngredients.addView(row)
    }

    private fun addStepRow(description: String = "") {
        val row = LayoutInflater.from(this).inflate(R.layout.item_step_row, llSteps, false)
        row.findViewById<EditText>(R.id.etStepDesc).setText(description)
        row.findViewById<Button>(R.id.btnRemoveStep).setOnClickListener { llSteps.removeView(row) }
        llSteps.addView(row)
    }

    private fun collectIngredients(recipeId: Long): List<Ingredient> =
        (0 until llIngredients.childCount).mapNotNull { i ->
            val row = llIngredients.getChildAt(i)
            val name = row.findViewById<EditText>(R.id.etIngName).text.toString().trim()
            if (name.isEmpty()) null
            else Ingredient(
                recipeId = recipeId,
                name = name,
                amount = row.findViewById<EditText>(R.id.etIngAmount).text.toString().trim(),
                unit = row.findViewById<EditText>(R.id.etIngUnit).text.toString().trim()
            )
        }

    private fun collectSteps(recipeId: Long): List<Step> =
        (0 until llSteps.childCount).mapIndexed { i, _ ->
            val row = llSteps.getChildAt(i)
            Step(
                recipeId = recipeId,
                orderIndex = i,
                description = row.findViewById<EditText>(R.id.etStepDesc).text.toString().trim()
            )
        }.filter { it.description.isNotEmpty() }

    private fun save() {
        val name = etName.text.toString().trim()
        if (name.isEmpty()) {
            etName.error = "Name is required"
            return
        }

        val recipe = Recipe(
            id = if (recipeId == -1L) 0 else recipeId,
            name = name,
            persons = etPersons.text.toString().trim().toIntOrNull(),
            timeMinutes = etTime.text.toString().trim().toIntOrNull(),
            categories = etCategories.text.toString().trim(),
            imageUri = selectedImageUri
        )

        // recipeId used for collecting child rows; real id is resolved inside the repository
        val tempId = if (recipeId == -1L) 0L else recipeId
        viewModel.save(recipe, collectIngredients(tempId), collectSteps(tempId))
    }
}
