package com.miseenplace.ui.list

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.miseenplace.R
import com.miseenplace.MiseEnPlace
import com.miseenplace.ui.detail.RecipeDetailActivity
import com.miseenplace.ui.edit.RecipeEditActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: RecipeListViewModel by viewModels {
        RecipeListViewModel.factory((application as MiseEnPlace).repository)
    }

    private lateinit var adapter: RecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Recipes"

        adapter = RecipeAdapter(emptyList()) { recipe ->
            startActivity(Intent(this, RecipeDetailActivity::class.java).apply {
                putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id)
            })
        }

        findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL))
            adapter = this@MainActivity.adapter
        }

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            startActivity(Intent(this, RecipeEditActivity::class.java))
        }

        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is RecipeListUiState.Loading -> Unit
                    is RecipeListUiState.Success -> adapter.update(state.recipes)
                    is RecipeListUiState.Error -> Toast.makeText(this@MainActivity, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadRecipes()
    }
}
