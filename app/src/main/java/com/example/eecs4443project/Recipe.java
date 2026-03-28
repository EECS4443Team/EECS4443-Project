package com.example.eecs4443project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a recipe with a title, image, ingredients, and instructions.
 * Used when parsing recipes from websites (HtmlParser) and when loading from the database.
 */
public class Recipe implements Serializable {
    private final long id;
    private final String title;
    private final List<String> ingredients;
    private final List<String> instructions;

    /**
     * Creates a Recipe from database data (with an ID).
     */
    public Recipe(long id, String title, List<String> ingredients, List<String> instructions) {
        this.id = id;
        this.title = title;
        this.ingredients = (ingredients != null) ? ingredients : new ArrayList<>();
        this.instructions = (instructions != null) ? instructions : new ArrayList<>();
    }

    /**
     * Creates a Recipe from website data (with an image URL).
     */
    public Recipe(String title, List<String> ingredients, List<String> instructions) {
        this.id = 0;
        this.title = title;
        this.ingredients = (ingredients != null) ? ingredients : new ArrayList<>();
        this.instructions = (instructions != null) ? instructions : new ArrayList<>();
    }

    // --- Getters ---

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    /**
     * Converts the recipe into a formatted text string.
     * This format matches the structure expected by RecipeParser.
     */
    public String toFormattedText() {
        StringBuilder text = new StringBuilder();

        // Add title
        text.append("Title: ");
        text.append(title != null ? title : "Untitled Recipe");
        text.append("\n\n");

        // Add ingredients
        text.append("Ingredients:\n");
        for (String ingredient : ingredients) {
            text.append("- ").append(ingredient).append("\n");
        }
        text.append("\n");

        // Add instructions
        text.append("Instructions:\n");
        for (int i = 0; i < instructions.size(); i++) {
            int stepNumber = i + 1;
            text.append(stepNumber).append(". ").append(instructions.get(i)).append("\n");
        }

        return text.toString();
    }
}
