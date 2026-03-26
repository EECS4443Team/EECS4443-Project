package com.example.eecs4443project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
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
    private static final String TAG = "AIResultsActivity";

    private ProgressBar progressBar;
    private TextView statusText;
    private LinearLayout recipeListContainer;
    private String generatedResponse = "";

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

        String query = getIntent().getStringExtra("query");
        boolean isSpicy = getIntent().getBooleanExtra("spicy", false);
        boolean isVegetarian = getIntent().getBooleanExtra("vegetarian", false);

        fetchRecipe(query, isSpicy, isVegetarian);
    }

    private void fetchRecipe(String ingredients, boolean isSpicy, boolean isVegetarian) {
        progressBar.setVisibility(View.VISIBLE);
        statusText.setVisibility(View.VISIBLE);
        statusText.setText("Gemini is cooking up your 10 recipes...");
        recipeListContainer.removeAllViews();

        Futures.addCallback(
                gemeniAPI.getRecipeFromAI(ingredients, isSpicy, isVegetarian),
                new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        generatedResponse = result.getText();
                        Log.d(TAG, "AI Success. Response length: " + (generatedResponse != null ? generatedResponse.length() : 0));

                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            statusText.setVisibility(View.GONE);
                            parseAndDisplayRecipes(generatedResponse);
                        });
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            statusText.setText("Error: " + t.getMessage());
                        });
                    }
                },
                ContextCompat.getMainExecutor(this)
        );
    }

    private void parseAndDisplayRecipes(String response) {
        if (response == null) return;

        String[] recipeSections = response.split("---");

        for (String section : recipeSections) {
            String trimmedSection = section.trim();
            if (trimmedSection.length() < 10) continue;

            String title = "New Recipe";
            String[] lines = trimmedSection.split("\n");

            for (String line : lines) {
                String cleanLine = line.trim().replaceAll("[\\*#]", "");
                if (cleanLine.toLowerCase().contains("title:")) {
                    title = cleanLine.substring(cleanLine.toLowerCase().indexOf("title:") + 6).trim();
                    break;
                }
            }

            if (title.equals("New Recipe") && lines.length > 0) {
                for(String line : lines) {
                    if (!line.trim().isEmpty()) {
                        title = line.trim().replaceAll("[\\*#]", "");
                        break;
                    }
                }
            }

            addRecipeCard(title, trimmedSection);
        }
    }

    private void addRecipeCard(String title, String fullText) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.item_recipe_card, recipeListContainer, false);

        TextView titleText = cardView.findViewById(R.id.recipeTitle);
        titleText.setText(title);

        // Crucial: Set the click listener on the ID we added to the XML
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