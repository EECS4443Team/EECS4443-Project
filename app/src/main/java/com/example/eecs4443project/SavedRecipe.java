package com.example.eecs4443project;

/**
 * Simple model class representing a recipe that has been saved to the database.
 * Stores the title, ingredients, and instructions as plain strings.
 */
public class SavedRecipe {
    private final String title;
    private final String ingredients;
    private final String instructions;

    public SavedRecipe(String title, String ingredients, String instructions) {
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }

    public String getTitle() {
        return title;
    }

    public String getIngredients() {
        return ingredients;
    }

    /**
     * Converts the saved recipe back into the full text format
     * that RecipeParser can understand (Title/Ingredients/Instructions sections).
     */
    public String toFullText() {
        return "Title: " + title
                + "\n\nIngredients:\n" + ingredients
                + "\n\nInstructions:\n" + instructions;
    }
}
