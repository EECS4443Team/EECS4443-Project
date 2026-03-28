package com.example.eecs4443project;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

public class RecipeStepScrollActivity extends AppCompatActivity {
    private LinearLayout stepsContainer;
    private float currentTextSize = 16f;
    private final float MIN_TEXT_SIZE = 10f;
    private final float MAX_TEXT_SIZE = 40f;
    private ScaleGestureDetector scaleGestureDetector;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_step_scroll);

        int handMode = getIntent().getIntExtra("handMode", 1);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        TextView stepTitle = findViewById(R.id.stepTitle);
        stepTitle.setText(R.string.cooking_instructions_title);

        stepsContainer = findViewById(R.id.stepsContainer);
        stepsContainer.removeAllViews();

        RecipeStepViewModel viewModel = new ViewModelProvider(this).get(RecipeStepViewModel.class);

        // Observe ingredients
        viewModel.getIngredients().observe(this, ingredients -> {
            if (ingredients != null && !ingredients.isEmpty()) {
                addIngredientsToView(ingredients, stepsContainer);
            }
        });

        // Observe steps
        viewModel.getSteps().observe(this, steps -> {
            if (steps != null) {
                for (int i = 0; i < steps.size(); i++) {
                    addStepToView(steps.get(i), i + 1, stepsContainer);
                }
            }
        });

        // Parse recipe (ViewModel skips if already parsed after rotation)
        String recipeText = getIntent().getStringExtra("recipe_text");
        if (recipeText != null) {
            viewModel.parseRecipeText(recipeText);
        }

        // Setup Pinch-to-Zoom for Two Hands Mode
        if (handMode == 2) {
            scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(@NonNull ScaleGestureDetector detector) {
                    currentTextSize *= detector.getScaleFactor();
                    currentTextSize = Math.max(MIN_TEXT_SIZE, Math.min(currentTextSize, MAX_TEXT_SIZE));
                    updateAllTextSizes();
                    return true;
                }
            });

            findViewById(R.id.scrollView2).setOnTouchListener((v, event) -> {
                scaleGestureDetector.onTouchEvent(event);
                return false;
            });
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

    private void updateAllTextSizes() {
        for (int i = 0; i < stepsContainer.getChildCount(); i++) {
            View itemView = stepsContainer.getChildAt(i);
            TextView textView = itemView.findViewById(R.id.stepText);
            if (textView != null) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentTextSize);
            }
        }
    }

    private void addIngredientsToView(String ingredients, LinearLayout container) {
        View ingredientsView = LayoutInflater.from(this).inflate(R.layout.item_recipe_step, container, false);
        TextView titleView = ingredientsView.findViewById(R.id.stepNumber);
        TextView contentView = ingredientsView.findViewById(R.id.stepText);

        titleView.setText(R.string.ingredients_with_amounts_title);
        contentView.setText(ingredients);

        container.addView(ingredientsView);
    }

    private void addStepToView(String text, int number, LinearLayout container) {
        View stepView = LayoutInflater.from(this).inflate(R.layout.item_recipe_step, container, false);
        TextView stepNumberView = stepView.findViewById(R.id.stepNumber);
        TextView stepTextView = stepView.findViewById(R.id.stepText);

        stepNumberView.setText(getString(R.string.step_format, number));
        stepTextView.setText(text);

        container.addView(stepView);
    }
}
