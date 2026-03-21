package com.example.eecs4443project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AIResultsActivity extends AppCompatActivity {

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

        String query = getIntent().getStringExtra("query");

        TextView aiOutput = findViewById(R.id.aiOutput);
        aiOutput.setText(R.string.ai_output_placeholder);

        findViewById(R.id.viewRecipeButton).setOnClickListener(v -> {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra("acquisition_mode", "ai");
            intent.putExtra("query", query);
            startActivity(intent);
        });
    }
}
