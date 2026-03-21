package com.example.eecs4443project;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RecipeStepSwipeActivity extends AppCompatActivity {

    private int currentStep = 0;
    private TextView stepIndicator;
    private TextView stepContent;
    private Button prevButton;
    private Button nextButton;

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

        updateStepDisplay();

        prevButton.setOnClickListener(v -> {
            if (currentStep > 0) {
                currentStep--;
                updateStepDisplay();
            }
        });

        nextButton.setOnClickListener(v -> {
            currentStep++;
            updateStepDisplay();
        });
    }

    private void updateStepDisplay() {
        stepIndicator.setText(getString(R.string.step_format, currentStep + 1));
        stepContent.setText(R.string.step_placeholder);
        prevButton.setEnabled(currentStep > 0);
    }
}
