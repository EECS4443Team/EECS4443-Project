package com.example.eecs4443project;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for the Saved Recipes screen.
 * Loads the list of saved recipes from the database and handles deletion.
 */
public class SavedRecipesViewModel extends AndroidViewModel {
    private final RecipeRepository repository;
    private final MutableLiveData<List<SavedRecipe>> savedRecipesList =
            new MutableLiveData<>(new ArrayList<>());

    public SavedRecipesViewModel(@NonNull Application application) {
        super(application);
        repository = new RecipeRepository(application);
    }

    public LiveData<List<SavedRecipe>> getSavedRecipesList() {
        return savedRecipesList;
    }

    /**
     * Loads all saved recipes from the database into the LiveData.
     */
    public void loadSavedRecipes() {
        savedRecipesList.setValue(repository.getAllSavedRecipes());
    }

    /**
     * Deletes a recipe from the database by its title.
     * The Activity handles removing it from the adapter for smooth animation.
     */
    public void deleteRecipeFromDatabase(String title) {
        repository.deleteRecipeByTitle(title);
    }
}
