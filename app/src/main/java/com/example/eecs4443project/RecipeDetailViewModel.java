package com.example.eecs4443project;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * ViewModel for the Recipe Detail screen.
 * Parses recipe text for display and manages the save/unsave state.
 */
public class RecipeDetailViewModel extends AndroidViewModel {
    private final RecipeRepository repository;

    private final MutableLiveData<String> recipeTitle = new MutableLiveData<>("AI Generated Recipe");
    private final MutableLiveData<String> recipeIngredients = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> isSaved = new MutableLiveData<>(false);

    private boolean alreadyParsed = false;

    public RecipeDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new RecipeRepository(application);
    }

    public LiveData<String> getRecipeTitle() {
        return recipeTitle;
    }

    public LiveData<String> getRecipeIngredients() {
        return recipeIngredients;
    }

    public LiveData<Boolean> getIsSaved() {
        return isSaved;
    }

    /**
     * Parses the recipe text and updates the title, ingredients, and saved state.
     * Only parses once — skips if already parsed (e.g. after screen rotation).
     */
    public void parseAndDisplayRecipe(String recipeText) {
        if (alreadyParsed) return;
        alreadyParsed = true;

        // Parse and display the title
        String title = RecipeParser.parseTitle(recipeText);
        recipeTitle.setValue(title);

        // Parse and display the ingredients
        String ingredients = RecipeParser.parseIngredientsForDisplay(recipeText);
        if (ingredients.isEmpty()) {
            // If parsing failed, show a cleaned-up version of the raw text
            recipeIngredients.setValue(RecipeParser.buildFallbackDisplay(recipeText));
        } else {
            recipeIngredients.setValue(ingredients);
        }

        // Check if this recipe is already saved in the database
        isSaved.setValue(repository.isRecipeSaved(title));
    }

    /**
     * Toggles the save state: saves the recipe if not saved, or removes it if already saved.
     */
    public void toggleSaveRecipe(String recipeText) {
        String title = recipeTitle.getValue();
        if (title == null) return;

        Boolean currentlySaved = isSaved.getValue();
        if (currentlySaved != null && currentlySaved) {
            // Currently saved — delete it
            repository.deleteRecipeByTitle(title);
            isSaved.setValue(false);
        } else {
            // Not saved — save it
            long savedId = repository.saveRecipe(recipeText);
            isSaved.setValue(savedId != -1);
        }
    }
}
