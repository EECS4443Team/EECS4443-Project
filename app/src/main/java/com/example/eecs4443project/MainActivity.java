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

import java.util.ArrayList;
import java.util.List;

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
        
        CheckBox checkChicken = findViewById(R.id.checkChicken);
        CheckBox checkRice = findViewById(R.id.checkRice);
        CheckBox checkTomato = findViewById(R.id.checkTomato);
        CheckBox checkPasta = findViewById(R.id.checkPasta);
        
        Button searchButton = findViewById(R.id.searchButton);

        acquisitionToggle.check(R.id.toggleManual);

        searchButton.setOnClickListener(v -> {
            StringBuilder queryBuilder = new StringBuilder(searchInput.getText().toString().trim());
            
            List<String> selectedIngredients = new ArrayList<>();
            if (checkChicken.isChecked()) selectedIngredients.add("Chicken");
            if (checkRice.isChecked()) selectedIngredients.add("Rice");
            if (checkTomato.isChecked()) selectedIngredients.add("Tomato");
            if (checkPasta.isChecked()) selectedIngredients.add("Pasta");
            
            for (String ingredient : selectedIngredients) {
                if (queryBuilder.length() > 0) queryBuilder.append(", ");
                queryBuilder.append(ingredient);
            }

            boolean isAI = acquisitionToggle.getCheckedButtonId() == R.id.toggleAI;
            String acquisitionMode = isAI ? "ai" : "local";

            Intent intent;
            if (isAI) {
                intent = new Intent(this, AIResultsActivity.class);
            } else {
                intent = new Intent(this, SearchResultsActivity.class);
            }
            intent.putExtra("query", queryBuilder.toString());
            intent.putExtra("acquisition_mode", acquisitionMode);
            intent.putExtra("spicy", spicyCheck.isChecked());
            intent.putExtra("vegetarian", vegetarianCheck.isChecked());
            startActivity(intent);
        });
    }
}
