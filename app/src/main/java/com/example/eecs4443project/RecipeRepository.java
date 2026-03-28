package com.example.eecs4443project;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository that acts as a single point of access for recipe data.
 * ViewModels use this class instead of talking to the database directly.
 */
public class RecipeRepository {
    private final RecipeDatabaseHelper databaseHelper;

    public RecipeRepository(Context context) {
        this.databaseHelper = new RecipeDatabaseHelper(context);
    }

    /**
     * Parses recipe text and saves it to the database.
     *
     * @param recipeText The raw recipe text to parse and save.
     * @return The row ID of the saved recipe, or -1 if saving failed.
     */
    public long saveRecipe(String recipeText) {
        RecipeParser.ParsedRecipe parsedRecipe = RecipeParser.parseForDatabase(recipeText);
        return databaseHelper.saveRecipe(
                parsedRecipe.title,
                parsedRecipe.ingredients,
                parsedRecipe.instructions
        );
    }

    /**
     * Checks if a recipe with the given title is already saved.
     */
    public boolean isRecipeSaved(String title) {
        return databaseHelper.isRecipeSaved(title);
    }

    /**
     * Deletes a saved recipe by its title.
     */
    public void deleteRecipeByTitle(String title) {
        databaseHelper.deleteRecipeByTitle(title);
    }

    /**
     * Loads all saved recipes from the database and returns them as a list.
     */
    public List<SavedRecipe> getAllSavedRecipes() {
        List<SavedRecipe> recipeList = new ArrayList<>();

        Cursor cursor = databaseHelper.getAllSavedRecipes();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int titleColumnIndex = cursor.getColumnIndex(RecipeDatabaseHelper.COLUMN_TITLE);
                int ingredientsColumnIndex = cursor.getColumnIndex(RecipeDatabaseHelper.COLUMN_INGREDIENTS);
                int instructionsColumnIndex = cursor.getColumnIndex(RecipeDatabaseHelper.COLUMN_INSTRUCTIONS);

                String title = cursor.getString(titleColumnIndex);
                String ingredients = cursor.getString(ingredientsColumnIndex);
                String instructions = cursor.getString(instructionsColumnIndex);

                recipeList.add(new SavedRecipe(title, ingredients, instructions));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return recipeList;
    }
}
