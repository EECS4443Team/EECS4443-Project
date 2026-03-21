package com.example.eecs4443project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButtonToggleGroup;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final String[] INGREDIENTS = {"Chicken", "Beef", "Pork", "Shrimp", "Salmon", "Potato", "Spinach", "Mushroom","Tomato", "Onion", "Pasta", "Rice", "Cheese", "Bacon", "Garlic"};

    private List<CheckBox> checkBoxList = new ArrayList<>();

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

        LinearLayout container = findViewById(R.id.ingredientsContainer);

        for (String ingredient : INGREDIENTS) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(ingredient);
            checkBox.setTag(ingredient);
            checkBoxList.add(checkBox);
            container.addView(checkBox);
        }

//        CheckBox checkChicken = findViewById(R.id.checkChicken);
//        CheckBox checkRice = findViewById(R.id.checkRice);
//        CheckBox checkTomato = findViewById(R.id.checkTomato);
//        CheckBox checkPasta = findViewById(R.id.checkPasta);
        
        Button searchButton = findViewById(R.id.searchButton);

        acquisitionToggle.check(R.id.toggleManual);

        searchButton.setOnClickListener(v -> {
            StringBuilder queryBuilder = new StringBuilder(searchInput.getText().toString().trim());
            JSONArray selectedIngredients = new JSONArray();
            for (CheckBox  checkBox : checkBoxList) {
                if (checkBox.isChecked()) {
                    Log.d(
                            "MainActivity",
                            "Selected ingredient: " + checkBox.getText().toString()
                    );
                    selectedIngredients.put(checkBox.getText().toString());
                }
            }


//            List<String> selectedIngredients = new ArrayList<>();
//            if (checkChicken.isChecked()) selectedIngredients.add("Chicken");
//            if (checkRice.isChecked()) selectedIngredients.add("Rice");
//            if (checkTomato.isChecked()) selectedIngredients.add("Tomato");
//            if (checkPasta.isChecked()) selectedIngredients.add("Pasta");
            
            for (String ingredient : INGREDIENTS) {
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
            //TODO : pass to HtmlParser
            //HtmlParser parser = new HtmlParser();
            // parser.searchRecipe(selectedIngredients);
            intent.putExtra("selected_ingredients", selectedIngredients.toString());
            intent.putExtra("query", queryBuilder.toString());
            intent.putExtra("acquisition_mode", acquisitionMode);
            intent.putExtra("spicy", spicyCheck.isChecked());
            intent.putExtra("vegetarian", vegetarianCheck.isChecked());
            startActivity(intent);
        });
    }
}
