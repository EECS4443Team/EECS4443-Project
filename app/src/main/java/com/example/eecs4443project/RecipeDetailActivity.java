package com.example.eecs4443project;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButtonToggleGroup;

public class RecipeDetailActivity extends AppCompatActivity {
    private static final String TAG = "RecipeDetailActivity";
    private RecipeDetailViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(RecipeDetailViewModel.class);

        TextView recipeTitle = findViewById(R.id.recipeTitle);
        TextView recipeIngredients = findViewById(R.id.recipeIngredients);
        MaterialButtonToggleGroup navToggle = findViewById(R.id.navToggle);
        Button startCookingButton = findViewById(R.id.startCookingButton);
        Button saveRecipeButton = findViewById(R.id.saveRecipeButton);

        // Get the recipe data from Intent
        String mode = getIntent().getStringExtra("acquisition_mode");
        String recipeText = getIntent().getStringExtra("recipe_text");
        boolean fromSaved = getIntent().getBooleanExtra("from_saved", false);

        Log.d(TAG, "Mode: " + mode);
        Log.d(TAG, "Recipe Text received: " + (recipeText != null ? "Yes" : "No"));

        // Observe ViewModel LiveData
        viewModel.getRecipeTitle().observe(this, recipeTitle::setText);
        viewModel.getRecipeIngredients().observe(this, recipeIngredients::setText);

        // Hide save button when viewing a saved recipe
        if (fromSaved) {
            saveRecipeButton.setVisibility(View.GONE);
        } else {
            viewModel.getIsSaved().observe(this, saved -> {
                if (saved) {
                    saveRecipeButton.setText(R.string.saved_recipe_button);
                    styleSaveButton(saveRecipeButton, true);
                } else {
                    saveRecipeButton.setText(R.string.save_recipe_button);
                    styleSaveButton(saveRecipeButton, false);
                }
            });
        }

        // Parse recipe text (ViewModel skips if already parsed after rotation)
        if (recipeText != null) {
            viewModel.parseAndDisplayRecipe(recipeText);
        }

        navToggle.check(R.id.toggleScroll);

        startCookingButton.setOnClickListener(v -> {
            boolean isCard = navToggle.getCheckedButtonId() == R.id.toggleCard;
            Intent intent;
            if (isCard) {
                intent = new Intent(this, RecipeStepSwipeActivity.class);
            } else {
                intent = new Intent(this, RecipeStepScrollActivity.class);
            }
            MaterialButtonToggleGroup handToggle = findViewById(R.id.handToggle);
            int selectedHandId = handToggle.getCheckedButtonId();
            if (selectedHandId == R.id.toggleOneHande) {
                intent.putExtra("handMode", 1);
            } else if (selectedHandId == R.id.toggleTwoHande) {
                intent.putExtra("handMode", 2);
            } else {
                // Default fallback if nothing is checked
                intent.putExtra("handMode", 1);
            }
            intent.putExtra("recipe_text", recipeText);
            startActivity(intent);
        });

        saveRecipeButton.setOnClickListener(v -> {
            if (recipeText == null) {
                Toast.makeText(this, "No recipe data to save", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.toggleSaveRecipe(recipeText);
            // LiveData observer already updated the button; show appropriate toast
            Boolean saved = viewModel.getIsSaved().getValue();
            if (saved != null && saved) {
                Toast.makeText(this, "Recipe saved successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Recipe unsaved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void styleSaveButton(Button button, boolean saved) {
        if (saved) {
            button.setBackgroundColor(Color.parseColor("#4A148C"));
            button.setTextColor(Color.WHITE);
        } else {
            button.setBackgroundColor(Color.TRANSPARENT);
            button.setTextColor(Color.parseColor("#4A148C"));
        }
    }
}
