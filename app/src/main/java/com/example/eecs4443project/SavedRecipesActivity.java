package com.example.eecs4443project;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SavedRecipesActivity extends AppCompatActivity {

    private RecipeDatabaseHelper dbHelper;
    private List<SavedRecipe> savedRecipesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_results); // Reusing search results layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView title = findViewById(R.id.resultsTitle);
        if (title != null) title.setText("Saved Recipes");

        dbHelper = new RecipeDatabaseHelper(this);
        loadSavedRecipes();

        RecyclerView recyclerView = findViewById(R.id.recipeRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new SavedRecipesAdapter(savedRecipesList));
    }

    private void loadSavedRecipes() {
        Cursor cursor = dbHelper.getAllSavedRecipes();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int titleIndex = cursor.getColumnIndex(RecipeDatabaseHelper.COLUMN_TITLE);
                int ingredientsIndex = cursor.getColumnIndex(RecipeDatabaseHelper.COLUMN_INGREDIENTS);
                int instructionsIndex = cursor.getColumnIndex(RecipeDatabaseHelper.COLUMN_INSTRUCTIONS);

                String title = cursor.getString(titleIndex);
                String ingredients = cursor.getString(ingredientsIndex);
                String instructions = cursor.getString(instructionsIndex);

                savedRecipesList.add(new SavedRecipe(title, ingredients, instructions));
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    static class SavedRecipe {
        String title;
        String ingredients;
        String instructions;

        SavedRecipe(String title, String ingredients, String instructions) {
            this.title = title;
            this.ingredients = ingredients;
            this.instructions = instructions;
        }
    }

    class SavedRecipesAdapter extends RecyclerView.Adapter<SavedRecipesAdapter.ViewHolder> {
        private List<SavedRecipe> recipes;

        SavedRecipesAdapter(List<SavedRecipe> recipes) {
            this.recipes = recipes;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SavedRecipe recipe = recipes.get(position);
            holder.recipeName.setText(recipe.title);
            holder.recipeImage.setImageResource(R.mipmap.ic_launcher);
            
            holder.itemView.setOnClickListener(v -> {
                // Construct the full recipe text as it was received from AI
                String fullText = "Title: " + recipe.title + "\n\nIngredients:\n" + recipe.ingredients + "\n\nInstructions:\n" + recipe.instructions;
                
                Intent intent = new Intent(SavedRecipesActivity.this, RecipeDetailActivity.class);
                intent.putExtra("acquisition_mode", "ai"); // Reuse the AI parsing logic
                intent.putExtra("recipe_text", fullText);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return recipes.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView recipeImage;
            TextView recipeName;

            ViewHolder(View itemView) {
                super(itemView);
                recipeImage = itemView.findViewById(R.id.recipeImage);
                recipeName = itemView.findViewById(R.id.recipeName);
            }
        }
    }
}
