package com.example.eecs4443project;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
    private SavedRecipesAdapter adapter;

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
        adapter = new SavedRecipesAdapter(savedRecipesList);
        recyclerView.setAdapter(adapter);
    }

    private void loadSavedRecipes() {
        savedRecipesList.clear();
        Cursor cursor = dbHelper.getAllSavedRecipes();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(RecipeDatabaseHelper.COLUMN_ID);
                int titleIndex = cursor.getColumnIndex(RecipeDatabaseHelper.COLUMN_TITLE);
                int ingredientsIndex = cursor.getColumnIndex(RecipeDatabaseHelper.COLUMN_INGREDIENTS);
                int instructionsIndex = cursor.getColumnIndex(RecipeDatabaseHelper.COLUMN_INSTRUCTIONS);

                long id = cursor.getLong(idIndex);
                String title = cursor.getString(titleIndex);
                String ingredients = cursor.getString(ingredientsIndex);
                String instructions = cursor.getString(instructionsIndex);

                savedRecipesList.add(new SavedRecipe(id, title, ingredients, instructions));
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    static class SavedRecipe {
        long id;
        String title;
        String ingredients;
        String instructions;

        SavedRecipe(long id, String title, String ingredients, String instructions) {
            this.id = id;
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
            holder.deleteButton.setVisibility(View.VISIBLE);

            holder.itemView.setOnClickListener(v -> {
                // Construct the full recipe text as it was received from AI
                String fullText = "Title: " + recipe.title + "\n\nIngredients:\n" + recipe.ingredients + "\n\nInstructions:\n" + recipe.instructions;
                
                Intent intent = new Intent(SavedRecipesActivity.this, RecipeDetailActivity.class);
                intent.putExtra("acquisition_mode", "ai"); // Reuse the AI parsing logic
                intent.putExtra("recipe_text", fullText);
                intent.putExtra("is_saved", true); // Flag to hide save button
                startActivity(intent);
            });

            holder.deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(SavedRecipesActivity.this)
                        .setTitle("Delete Recipe")
                        .setMessage("Are you sure you want to delete this recipe?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            dbHelper.deleteRecipe(recipe.id);
                            loadSavedRecipes();
                            notifyDataSetChanged();
                            Toast.makeText(SavedRecipesActivity.this, "Recipe deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return recipes.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView recipeImage;
            TextView recipeName;
            ImageButton deleteButton;

            ViewHolder(View itemView) {
                super(itemView);
                recipeImage = itemView.findViewById(R.id.recipeImage);
                recipeName = itemView.findViewById(R.id.recipeName);
                deleteButton = itemView.findViewById(R.id.deleteRecipeButton);
            }
        }
    }
}
