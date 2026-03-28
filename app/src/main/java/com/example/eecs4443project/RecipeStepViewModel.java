package com.example.eecs4443project;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel shared by RecipeStepScrollActivity and RecipeStepSwipeActivity.
 * Parses recipe text into ingredients and a list of cooking steps.
 */
public class RecipeStepViewModel extends ViewModel {
    private final MutableLiveData<String> ingredients = new MutableLiveData<>();
    private final MutableLiveData<List<String>> steps = new MutableLiveData<>();
    private boolean alreadyParsed = false;

    public LiveData<String> getIngredients() {
        return ingredients;
    }

    public LiveData<List<String>> getSteps() {
        return steps;
    }

    /**
     * Parses the recipe text into ingredients and cooking steps.
     * Only parses once — skips if already parsed (e.g. after screen rotation).
     */
    public void parseRecipeText(String recipeText) {
        if (alreadyParsed) return;
        alreadyParsed = true;

        // Parse ingredients
        ingredients.setValue(RecipeParser.parseIngredientsWithAmounts(recipeText));

        // Parse cooking steps
        List<String> parsedSteps = RecipeParser.parseSteps(recipeText);
        if (parsedSteps.isEmpty() && recipeText != null) {
            // If no steps were found, show a fallback message
            List<String> fallbackSteps = new ArrayList<>();
            fallbackSteps.add("Follow the instructions provided in the recipe details.");
            steps.setValue(fallbackSteps);
        } else {
            steps.setValue(parsedSteps);
        }
    }
}
