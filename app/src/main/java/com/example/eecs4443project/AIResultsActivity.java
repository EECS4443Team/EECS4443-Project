package com.example.eecs4443project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

public class AIResultsActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView statusText;
    private CardView recipeCard;
    private TextView recipeTitle;
    private String generatedRecipe = "";
    private String recipeTitleText = "";

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
        recipeCard = findViewById(R.id.recipeCard);
        recipeTitle = findViewById(R.id.recipeTitle);

        String query = getIntent().getStringExtra("query");
        boolean isSpicy = getIntent().getBooleanExtra("spicy", false);
        boolean isVegetarian = getIntent().getBooleanExtra("vegetarian", false);

        fetchRecipe(query, isSpicy, isVegetarian);

        findViewById(R.id.recipeItemContainer).setOnClickListener(v -> {
            if (generatedRecipe != null && !generatedRecipe.isEmpty()) {
                Intent intent = new Intent(this, RecipeDetailActivity.class);
                intent.putExtra("acquisition_mode", "ai");
                intent.putExtra("recipe_text", generatedRecipe);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please wait for the recipe to load...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRecipe(String ingredients, boolean isSpicy, boolean isVegetarian) {
        progressBar.setVisibility(View.VISIBLE);
        statusText.setVisibility(View.VISIBLE);
        statusText.setText("Gemini is cooking up a recipe...");
        recipeCard.setVisibility(View.GONE);

        Futures.addCallback(
                gemeniAPI.getRecipeFromAI(ingredients, isSpicy, isVegetarian),
                new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        generatedRecipe = result.getText();
                        
                        // Extract title for display
                        recipeTitleText = "New Recipe Found!";
                        if (generatedRecipe != null) {
                            String[] lines = generatedRecipe.split("\n");
                            for (String line : lines) {
                                String cleanLine = line.trim().replaceAll("[\\*#]", "");
                                if (cleanLine.toLowerCase().startsWith("title:")) {
                                    recipeTitleText = cleanLine.substring(6).trim();
                                    break;
                                } else if (cleanLine.toLowerCase().startsWith("recipe name:")) {
                                    recipeTitleText = cleanLine.substring(12).trim();
                                    break;
                                }
                            }
                        }

                        runOnUiThread(() -> {
                            recipeTitle.setText(recipeTitleText);
                            progressBar.setVisibility(View.GONE);
                            statusText.setVisibility(View.GONE);
                            recipeCard.setVisibility(View.VISIBLE);
                        });
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            statusText.setText("Error generating recipe: " + t.getMessage());
                        });
                        t.printStackTrace();
                    }
                },
                ContextCompat.getMainExecutor(this)
        );
    }
}
