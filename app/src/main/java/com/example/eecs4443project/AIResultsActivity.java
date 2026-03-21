package com.example.eecs4443project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

public class AIResultsActivity extends AppCompatActivity {

    private TextView aiOutput;
    private ProgressBar progressBar;
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

        aiOutput = findViewById(R.id.aiOutput);
        progressBar = findViewById(R.id.progressBar);

        String query = getIntent().getStringExtra("query");
        boolean isSpicy = getIntent().getBooleanExtra("spicy", false);
        boolean isVegetarian = getIntent().getBooleanExtra("vegetarian", false);

        fetchRecipe(query, isSpicy, isVegetarian);

        findViewById(R.id.viewRecipeButton).setOnClickListener(v -> {
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
        aiOutput.setText("Gemini is cooking up a recipe...");

        Futures.addCallback(
                gemeniAPI.getRecipeFromAI(ingredients, isSpicy, isVegetarian),
                new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        generatedRecipe = result.getText();
                        
                        // Extract title for display in the results screen
                        recipeTitleText = "New Recipe Found!";
                        if (generatedRecipe != null) {
                            String[] lines = generatedRecipe.split("\n");
                            for (String line : lines) {
                                if (line.toLowerCase().contains("title:")) {
                                    recipeTitleText = line.replaceAll("(?i)^.*title:\\s*", "").replaceAll("[\\*#]", "").trim();
                                    break;
                                }
                            }
                        }

                        runOnUiThread(() -> {
                            aiOutput.setText(recipeTitleText + "\n\nClick 'View Recipe' below to see full details and start cooking!");
                            progressBar.setVisibility(View.GONE);
                        });
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            aiOutput.setText("Error generating recipe: " + t.getMessage());
                        });
                        t.printStackTrace();
                    }
                },
                ContextCompat.getMainExecutor(this)
        );
    }
}
