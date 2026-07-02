package com.miseenplace.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.miseenplace.R
import com.miseenplace.MiseEnPlace
import com.miseenplace.data.RecipeWithDetails
import com.miseenplace.ui.edit.RecipeEditActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecipeDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_RECIPE_ID = "recipe_id"
    }

    private val viewModel: RecipeDetailViewModel by viewModels {
        RecipeDetailViewModel.factory((application as MiseEnPlace).repository)
    }

    private var recipeId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        recipeId = intent.getLongExtra(EXTRA_RECIPE_ID, -1)

        findViewById<Button>(R.id.btnEdit).setOnClickListener {
            startActivity(Intent(this, RecipeEditActivity::class.java).apply {
                putExtra(RecipeEditActivity.EXTRA_RECIPE_ID, recipeId)
            })
        }

        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Recipe")
                .setMessage("Are you sure you want to delete this recipe?")
                .setPositiveButton("Delete") { _, _ -> viewModel.deleteRecipe(recipeId) }
                .setNegativeButton("Cancel", null)
                .show()
        }

        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is RecipeDetailUiState.Loading -> Unit
                    is RecipeDetailUiState.Success -> bind(state.details)
                    is RecipeDetailUiState.Deleted -> finish()
                    is RecipeDetailUiState.Error -> {
                        Toast.makeText(this@RecipeDetailActivity, state.message, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadRecipe(recipeId)
    }

    private fun bind(details: RecipeWithDetails) {
        val r = details.recipe
        title = r.name

        findViewById<TextView>(R.id.tvName).text = r.name
        findViewById<TextView>(R.id.tvPersons).text = r.persons?.toString() ?: "—"
        findViewById<TextView>(R.id.tvTime).text = if (r.timeMinutes != null) "${r.timeMinutes} min" else "—"
        findViewById<TextView>(R.id.tvCategories).text = r.categories.ifEmpty { "—" }

        val imgView = findViewById<ImageView>(R.id.imgRecipe)
        if (r.imageUri != null) {
            imgView.visibility = View.VISIBLE
            imgView.setImageURI(Uri.parse(r.imageUri))
        } else {
            imgView.visibility = View.GONE
        }

        val llIngredients = findViewById<LinearLayout>(R.id.llIngredients)
        llIngredients.removeAllViews()
        details.ingredients.forEach { ing ->
            val tv = TextView(this)
            val suffix = buildString {
                if (ing.amount.isNotEmpty()) append(ing.amount)
                if (ing.unit.isNotEmpty()) append(" ${ing.unit}")
            }
            tv.text = if (suffix.isNotEmpty()) "• ${ing.name}  ($suffix)" else "• ${ing.name}"
            tv.textSize = 15f
            tv.setPadding(0, 4, 0, 4)
            llIngredients.addView(tv)
        }

        val llSteps = findViewById<LinearLayout>(R.id.llSteps)
        llSteps.removeAllViews()
        details.steps.sortedBy { it.orderIndex }.forEachIndexed { i, step ->
            val tv = TextView(this)
            tv.text = "${i + 1}.  ${step.description}"
            tv.textSize = 15f
            tv.setPadding(0, 6, 0, 6)
            llSteps.addView(tv)
        }
    }
}
