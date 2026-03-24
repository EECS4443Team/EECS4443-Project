package com.example.eecs4443project;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class RecipeStepScrollActivity extends AppCompatActivity {
    private static final String TAG = "RecipeStepScroll";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_step_scroll);
        
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        TextView stepTitle = findViewById(R.id.stepTitle);
        stepTitle.setText("Cooking Instructions");

        LinearLayout stepsContainer = findViewById(R.id.stepsContainer);
        stepsContainer.removeAllViews();

        String recipeText = getIntent().getStringExtra("recipe_text");
        Log.d(TAG, "Recipe Text: " + recipeText);

        if (recipeText != null) {
            String ingredients = parseIngredients(recipeText);
            if (!ingredients.isEmpty()) {
                addIngredientsToView(ingredients, stepsContainer);
            }

            List<String> steps = parseSteps(recipeText);
            if (steps.isEmpty()) {
                // Fallback if steps parsing failed, try to show something
                addStepToView("Follow the instructions provided in the recipe details.", 1, stepsContainer);
            } else {
                for (int i = 0; i < steps.size(); i++) {
                    addStepToView(steps.get(i), i + 1, stepsContainer);
                }
            }
        }
        
        View topButton = findViewById(R.id.topButton);
        if (topButton != null) {
            topButton.setOnClickListener(v -> {
                View scrollView = findViewById(R.id.scrollView2);
                if (scrollView != null) {
                    scrollView.scrollTo(0, 0);
                }
            });
        }
    }

    private String parseIngredients(String text) {
        String[] lines = text.split("\n");
        StringBuilder ingredientsBuilder = new StringBuilder();
        boolean inIngredients = false;

        for (String line : lines) {
            String cleanLine = line.trim().replaceAll("[\\*#]", "");
            String lowerLine = cleanLine.toLowerCase();

            // Detect ingredients header more robustly (with or without colon)
            if (lowerLine.contains("ingredients")) {
                inIngredients = true;
                int colonIdx = cleanLine.indexOf(":");
                if (colonIdx != -1 && colonIdx < cleanLine.length() - 1) {
                    String restOfLine = cleanLine.substring(colonIdx + 1).trim();
                    if (!restOfLine.isEmpty()) {
                        ingredientsBuilder.append("• ").append(restOfLine).append("\n");
                    }
                }
                continue;
            }

            // Detect end of ingredients section
            if (lowerLine.contains("instructions") || lowerLine.contains("steps") || lowerLine.contains("directions")) {
                inIngredients = false;
                continue;
            }

            if (inIngredients && !cleanLine.isEmpty()) {
                // Prepend bullet point if not already present
                if (!cleanLine.startsWith("•") && !cleanLine.startsWith("-")) {
                    ingredientsBuilder.append("• ").append(cleanLine).append("\n");
                } else {
                    ingredientsBuilder.append(cleanLine).append("\n");
                }
            }
        }
        return ingredientsBuilder.toString().trim();
    }

    private List<String> parseSteps(String text) {
        List<String> steps = new ArrayList<>();
        String[] lines = text.split("\n");
        boolean inInstructions = false;
        StringBuilder currentStep = new StringBuilder();

        for (String line : lines) {
            String cleanLine = line.trim().replaceAll("[\\*#]", "");
            String lowerLine = cleanLine.toLowerCase();

            if (lowerLine.contains("instructions") || lowerLine.contains("steps") || lowerLine.contains("directions")) {
                inInstructions = true;
                int colonIdx = cleanLine.indexOf(":");
                if (colonIdx != -1 && colonIdx < cleanLine.length() - 1) {
                    String restOfLine = cleanLine.substring(colonIdx + 1).trim();
                    if (!restOfLine.isEmpty()) currentStep.append(restOfLine);
                }
                continue;
            }

            // If we hit another major section, stop parsing instructions
            if (inInstructions && (lowerLine.contains("ingredients") || lowerLine.contains("notes") || lowerLine.contains("prep time"))) {
                if (currentStep.length() > 0) {
                    steps.add(currentStep.toString().trim());
                    currentStep.setLength(0);
                }
                inInstructions = false;
                continue;
            }

            if (inInstructions && !cleanLine.isEmpty()) {
                // Check if line starts with a step number like "1." or "1)"
                if (cleanLine.matches("^\\d+[\\.\\)].*")) {
                    if (currentStep.length() > 0) {
                        steps.add(currentStep.toString().trim());
                        currentStep.setLength(0);
                    }
                    // Remove the "1. " prefix and add the rest
                    currentStep.append(cleanLine.replaceAll("^\\d+[\\.\\)]\\s*", ""));
                } else {
                    // Append to current step if it's a continuation line
                    if (currentStep.length() > 0) currentStep.append(" ");
                    currentStep.append(cleanLine);
                }
            }
        }

        if (currentStep.length() > 0) {
            steps.add(currentStep.toString().trim());
        }

        return steps;
    }

    private void addIngredientsToView(String ingredients, LinearLayout container) {
        View ingredientsView = LayoutInflater.from(this).inflate(R.layout.item_recipe_step, container, false);
        TextView titleView = ingredientsView.findViewById(R.id.stepNumber);
        TextView contentView = ingredientsView.findViewById(R.id.stepText);

        titleView.setText("Ingredients with Amounts");
        contentView.setText(ingredients);

        container.addView(ingredientsView);
    }

    private void addStepToView(String text, int number, LinearLayout container) {
        View stepView = LayoutInflater.from(this).inflate(R.layout.item_recipe_step, container, false);
        TextView stepNumberView = stepView.findViewById(R.id.stepNumber);
        TextView stepTextView = stepView.findViewById(R.id.stepText);

        stepNumberView.setText("Step " + number);
        stepTextView.setText(text);

        container.addView(stepView);
    }
}
