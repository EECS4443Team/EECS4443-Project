package com.example.eecs4443project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

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

        MaterialButtonToggleGroup navToggle = findViewById(R.id.navToggle);
        Button startCookingButton = findViewById(R.id.startCookingButton);

        navToggle.check(R.id.toggleScroll);

        startCookingButton.setOnClickListener(v -> {
            boolean isCard = navToggle.getCheckedButtonId() == R.id.toggleCard;
            Intent intent;
            if (isCard) {
                intent = new Intent(this, RecipeStepSwipeActivity.class);
            } else {
                intent = new Intent(this, RecipeStepScrollActivity.class);
            }
            startActivity(intent);
        });
    }
}
