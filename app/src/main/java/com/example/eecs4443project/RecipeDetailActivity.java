package com.example.eecs4443project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButtonToggleGroup;

public class RecipeDetailActivity extends AppCompatActivity {

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

        TextView recipeTitle = findViewById(R.id.recipeTitle);
        TextView recipeIngredients = findViewById(R.id.recipeIngredients);
        MaterialButtonToggleGroup navToggle = findViewById(R.id.navToggle);
        Button startCookingButton = findViewById(R.id.startCookingButton);

        // Get the recipe data from Intent
        String mode = getIntent().getStringExtra("acquisition_mode");
        String recipeText = getIntent().getStringExtra("recipe_text");

        if ("ai".equals(mode) && recipeText != null) {
            // Simple parsing for AI output
            String[] lines = recipeText.split("\n");
            String title = "AI Generated Recipe";
            StringBuilder ingredients = new StringBuilder();

            boolean foundTitle = false;
            for (String line : lines) {
                if (line.toLowerCase().startsWith("title:") && !foundTitle) {
                    title = line.substring(6).trim();
                    foundTitle = true;
                } else {
                    ingredients.append(line).append("\n");
                }
            }

            recipeTitle.setText(title);
            recipeIngredients.setText(ingredients.toString().trim());
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
            // Pass the recipe text forward if needed
            intent.putExtra("recipe_text", recipeText);
            startActivity(intent);
        });
    }
}
