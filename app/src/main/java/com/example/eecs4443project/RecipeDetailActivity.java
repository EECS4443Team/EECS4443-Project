package com.example.eecs4443project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButtonToggleGroup;

public class RecipeDetailActivity extends AppCompatActivity {
    private static final String TAG = "RecipeDetailActivity";
    private RecipeDatabaseHelper dbHelper;

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

        dbHelper = new RecipeDatabaseHelper(this);

        TextView recipeTitle = findViewById(R.id.recipeTitle);
        TextView recipeIngredients = findViewById(R.id.recipeIngredients);
        MaterialButtonToggleGroup navToggle = findViewById(R.id.navToggle);
        Button startCookingButton = findViewById(R.id.startCookingButton);
        Button saveRecipeButton = findViewById(R.id.saveRecipeButton);

        // Get the recipe data from Intent
        String mode = getIntent().getStringExtra("acquisition_mode");
        String recipeText = getIntent().getStringExtra("recipe_text");

        Log.d(TAG, "Mode: " + mode);
        Log.d(TAG, "Recipe Text received: " + (recipeText != null ? "Yes" : "No"));

        if (recipeText != null) {

            parseAndDisplayRecipe(recipeText, recipeTitle, recipeIngredients);
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
            if (recipeText != null) {
                saveRecipeToDatabase(recipeText);
            } else {
                Toast.makeText(this, "No recipe data to save", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveRecipeToDatabase(String text) {
        String title = "AI Generated Recipe";
        StringBuilder ingredients = new StringBuilder();
        StringBuilder instructions = new StringBuilder();
        
        String[] lines = text.split("\n");
        boolean inIngredients = false;
        boolean inInstructions = false;

        for (String line : lines) {
            String cleanLine = line.trim().replaceAll("[\\*#]", "");
            String lowerLine = cleanLine.toLowerCase();

            if (lowerLine.contains("title:")) {
                title = cleanLine.substring(lowerLine.indexOf("title:") + 6).trim();
            } else if (lowerLine.startsWith("recipe #")) {
                int colonIndex = cleanLine.indexOf(":");
                if (colonIndex != -1) {
                    title = cleanLine.substring(colonIndex + 1).trim();
                }
            } else if (lowerLine.contains("ingredients:")) {
                inIngredients = true;
                inInstructions = false;
                String restOfLine = cleanLine.substring(lowerLine.indexOf("ingredients:") + 12).trim();
                if (!restOfLine.isEmpty()) {
                    ingredients.append(restOfLine).append("\n");
                }
            } else if (lowerLine.contains("instructions:") || lowerLine.contains("steps:") || lowerLine.contains("directions:")) {
                inIngredients = false;
                inInstructions = true;
                int colonIdx = lowerLine.indexOf("instructions:");
                if (colonIdx == -1) colonIdx = lowerLine.indexOf("steps:");
                if (colonIdx == -1) colonIdx = lowerLine.indexOf("directions:");
                
                String restOfLine = cleanLine.substring(cleanLine.indexOf(":") + 1).trim();
                if (!restOfLine.isEmpty()) {
                    instructions.append(restOfLine).append("\n");
                }
            } else if (inIngredients && !cleanLine.isEmpty()) {
                ingredients.append(cleanLine).append("\n");
            } else if (inInstructions && !cleanLine.isEmpty()) {
                instructions.append(cleanLine).append("\n");
            }
        }

        long id = dbHelper.saveRecipe(title, ingredients.toString().trim(), instructions.toString().trim());
        if (id != -1) {
            Toast.makeText(this, "Recipe saved successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to save recipe", Toast.LENGTH_SHORT).show();
        }
    }
    private void parseAndDisplayRecipe(String text, TextView titleView, TextView ingredientsView) {
        String[] lines = text.split("\n");
        String title  = "AI Generated Recipe";
        StringBuilder ingredients = new StringBuilder();
        
        boolean inIngredients = false;

        for (String line : lines) {
            String cleanLine = line.trim().replaceAll("[\\*#]", "");
            String lowerLine = cleanLine.toLowerCase();

            // Skip preamble lines
            if (lowerLine.contains("here are 10") || lowerLine.contains("brief and concise")) {
                continue;
            }

            if (lowerLine.contains("title:")) {
                title = cleanLine.substring(lowerLine.indexOf("title:") + 6).trim();
            } else if (lowerLine.startsWith("recipe #")) {
                int colonIndex = cleanLine.indexOf(":");
                if (colonIndex != -1) {
                    title = cleanLine.substring(colonIndex + 1).trim();
                }
            } else if (lowerLine.contains("ingredients:")) {
                inIngredients = true;
                String restOfLine = cleanLine.substring(lowerLine.indexOf("ingredients:") + 12).trim();
                if (!restOfLine.isEmpty()) {
                    ingredients.append("• ").append(restOfLine).append("\n");
                }
            } else if (lowerLine.contains("instructions:")) {
                inIngredients = false;
            } else if (inIngredients && !cleanLine.isEmpty()) {
                if (!lowerLine.contains("prep time:") && !lowerLine.contains("recipe #")) {
                    ingredients.append("• ").append(cleanLine).append("\n");
                }
            }
        }

        titleView.setText(title);
        if (ingredients.length() > 0) {
            ingredientsView.setText(ingredients.toString().trim());
        } else {
            // Fallback: clean the text but keep structure if parsing failed
            StringBuilder fallback = new StringBuilder();
            for (String line : lines) {
                String clean = line.trim();
                if (!clean.toLowerCase().contains("here are 10") && !clean.isEmpty()) {
                    fallback.append(clean).append("\n");
                }
            }
            ingredientsView.setText(fallback.toString().trim());
        }
    }
}
