package com.example.eecs4443project;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class RecipeStepSwipeActivity extends AppCompatActivity {
    private static final String TAG = "RecipeStepSwipe";

    private int currentStep = 0;
    private TextView stepIndicator;
    private TextView stepContent;
    private Button prevButton;
    private Button nextButton;
    private SeekBar seekBar;
    private List<String> steps = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_step_swipe);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        stepIndicator = findViewById(R.id.stepIndicator);
        stepContent = findViewById(R.id.stepContent);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        seekBar = findViewById(R.id.seekBar);

        String recipeText = getIntent().getStringExtra("recipe_text");
        if (recipeText != null) {
            String ingredients = parseIngredients(recipeText);
            if (!ingredients.isEmpty()) {
                steps.add("Ingredients with Amounts:\n\n" + ingredients);
            }
            steps.addAll(parseSteps(recipeText));
        }

        if (steps.isEmpty()) {
            steps.add("Follow the instructions provided in the recipe details.");
        }

        seekBar.setMax(steps.size() - 1);
        updateStepDisplay();

        prevButton.setOnClickListener(v -> {
            if (currentStep > 0) {
                currentStep--;
                updateStepDisplay();
            }
        });

        nextButton.setOnClickListener(v -> {
            if (currentStep < steps.size() - 1) {
                currentStep++;
                updateStepDisplay();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentStep = progress;
                    updateStepDisplay();
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateStepDisplay() {
        if (currentStep == 0 && steps.get(0).startsWith("Ingredients")) {
            stepIndicator.setText("Ingredients");
        } else {
            int stepNum = steps.get(0).startsWith("Ingredients") ? currentStep : currentStep + 1;
            stepIndicator.setText("Step " + stepNum);
        }
        stepContent.setText(steps.get(currentStep));
        prevButton.setEnabled(currentStep > 0);
        nextButton.setEnabled(currentStep < steps.size() - 1);
        seekBar.setProgress(currentStep);
    }

    private String parseIngredients(String text) {
        String[] lines = text.split("\n");
        StringBuilder ingredientsBuilder = new StringBuilder();
        boolean inIngredients = false;

        for (String line : lines) {
            String cleanLine = line.trim().replaceAll("[\\*#]", "");
            String lowerLine = cleanLine.toLowerCase();

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

            if (lowerLine.contains("instructions") || lowerLine.contains("steps") || lowerLine.contains("directions")) {
                inIngredients = false;
                continue;
            }

            if (inIngredients && !cleanLine.isEmpty()) {
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
        List<String> stepsList = new ArrayList<>();
        String[] lines = text.split("\n");
        boolean inInstructions = false;
        StringBuilder currentStepBuilder = new StringBuilder();

        for (String line : lines) {
            String cleanLine = line.trim().replaceAll("[\\*#]", "");
            String lowerLine = cleanLine.toLowerCase();

            if (lowerLine.contains("instructions") || lowerLine.contains("steps") || lowerLine.contains("directions")) {
                inInstructions = true;
                int colonIdx = cleanLine.indexOf(":");
                if (colonIdx != -1 && colonIdx < cleanLine.length() - 1) {
                    String restOfLine = cleanLine.substring(colonIdx + 1).trim();
                    if (!restOfLine.isEmpty()) currentStepBuilder.append(restOfLine);
                }
                continue;
            }

            if (inInstructions && (lowerLine.contains("ingredients") || lowerLine.contains("notes") || lowerLine.contains("prep time"))) {
                if (currentStepBuilder.length() > 0) {
                    stepsList.add(currentStepBuilder.toString().trim());
                    currentStepBuilder.setLength(0);
                }
                inInstructions = false;
                continue;
            }

            if (inInstructions && !cleanLine.isEmpty()) {
                if (cleanLine.matches("^\\d+[\\.\\)].*")) {
                    if (currentStepBuilder.length() > 0) {
                        stepsList.add(currentStepBuilder.toString().trim());
                        currentStepBuilder.setLength(0);
                    }
                    currentStepBuilder.append(cleanLine.replaceAll("^\\d+[\\.\\)]\\s*", ""));
                } else {
                    if (currentStepBuilder.length() > 0) currentStepBuilder.append(" ");
                    currentStepBuilder.append(cleanLine);
                }
            }
        }

        if (currentStepBuilder.length() > 0) {
            stepsList.add(currentStepBuilder.toString().trim());
        }
        return stepsList;
    }
}
