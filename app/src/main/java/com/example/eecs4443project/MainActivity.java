package com.example.eecs4443project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButtonToggleGroup;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialButtonToggleGroup acquisitionToggle = findViewById(R.id.acquisitionToggle);
        EditText searchInput = findViewById(R.id.searchInput);
        CheckBox spicyCheck = findViewById(R.id.spicyCheck);
        CheckBox vegetarianCheck = findViewById(R.id.vegetarianCheck);
        Button searchButton = findViewById(R.id.searchButton);

        acquisitionToggle.check(R.id.toggleManual);

        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            boolean isAI = acquisitionToggle.getCheckedButtonId() == R.id.toggleAI;
            String acquisitionMode = isAI ? "ai" : "local";

            Intent intent;
            if (isAI) {
                intent = new Intent(this, AIResultsActivity.class);
            } else {
                intent = new Intent(this, SearchResultsActivity.class);
            }
            intent.putExtra("query", query);
            intent.putExtra("acquisition_mode", acquisitionMode);
            intent.putExtra("spicy", spicyCheck.isChecked());
            intent.putExtra("vegetarian", vegetarianCheck.isChecked());
            startActivity(intent);
        });
    }
}
