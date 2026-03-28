package com.example.eecs4443project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

public class AIResultsActivity extends AppCompatActivity {
    private static final String TAG = "AIResultsActivity";

    private ProgressBar progressBar;
    private TextView statusText;
    private LinearLayout recipeListContainer;
    private AIResultsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ai_results);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        progressBar = findViewById(R.id.progressBar);
        statusText = findViewById(R.id.statusText);
        recipeListContainer = findViewById(R.id.recipeListContainer);

        viewModel = new ViewModelProvider(this).get(AIResultsViewModel.class);

        // Observe loading state
        viewModel.getIsLoading().observe(this, loading -> {
            if (loading) {
                progressBar.setVisibility(View.VISIBLE);
                statusText.setVisibility(View.VISIBLE);
                statusText.setText("Gemini is cooking up your 10 recipes...");
                recipeListContainer.removeAllViews();
            } else {
                progressBar.setVisibility(View.GONE);
                statusText.setVisibility(View.GONE);
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                progressBar.setVisibility(View.GONE);
                statusText.setVisibility(View.VISIBLE);
                statusText.setText(error);
            }
        });

        // Observe parsed recipe sections
        viewModel.getRecipeSections().observe(this, sections -> {
            recipeListContainer.removeAllViews();
            if (sections.isEmpty()) {
                statusText.setVisibility(View.VISIBLE);
                statusText.setText("No results found.");
            } else {
                for (RecipeParser.RecipeSection section : sections) {
                    addRecipeCard(section.title, section.fullText);
                }
            }
        });

        // Fetch recipes (ViewModel handles deduplication on rotation)
        String query = getIntent().getStringExtra("query");
        boolean isSpicy = getIntent().getBooleanExtra("spicy", false);
        boolean isVegetarian = getIntent().getBooleanExtra("vegetarian", false);
        viewModel.fetchRecipes(query, isSpicy, isVegetarian);
    }

    private void addRecipeCard(String title, String fullText) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.item_recipe_card, recipeListContainer, false);

        TextView titleText = cardView.findViewById(R.id.recipeTitle);
        titleText.setText(title);

        View clickableArea = cardView.findViewById(R.id.recipeCardContent);
        if (clickableArea != null) {
            clickableArea.setOnClickListener(v -> {
                Log.d(TAG, "Recipe clicked: " + title);
                Intent intent = new Intent(AIResultsActivity.this, RecipeDetailActivity.class);
                intent.putExtra("acquisition_mode", "ai");
                intent.putExtra("recipe_text", fullText);
                startActivity(intent);
            });
        }

        recipeListContainer.addView(cardView);
    }
}
