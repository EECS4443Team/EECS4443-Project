package com.example.eecs4443project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Recipe implements Serializable {
    public long id;
    public String title;
    public List<String> ingredients;
    public List<String> instructions;
    public String imageUrl;
    private final Map<String, BiConsumer<String, String>> recipeMap = new HashMap<>(){{
        put("title", (cleanLine, lowerLine) -> processTitle(cleanLine, lowerLine));
        put("ingredients", (cleanLine, lowerLine) -> processIngredients(cleanLine, lowerLine));
        put("instructions", (cleanLine, lowerLine) -> processInstructions(cleanLine, lowerLine));
    }};

    private boolean inIngredients;
    public String getTitle() {
        return title;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    /**
     *Create Recipe including id
     */
    public Recipe(long id, String title, List<String> ingredients, List<String> instructions) {
        this.id = id;
        this.title = title;
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
        this.instructions = instructions != null ? instructions : new ArrayList<>();
    }
    /**
     *Create Recipe (default)
     */
    public Recipe(String title, String imageUrl, List<String> ingredients, List<String> instructions) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
        this.instructions = instructions != null ? instructions : new ArrayList<>();
    }

    /**
     *Create Recipe by extracting from the text
     */
    public Recipe(String text) {
        String[] lines = text.split("\n");
        String title = "AI Generated Recipe";
        inIngredients = false;

        for (String line : lines) {
            String cleanLine = line.trim().replaceAll("[\\*#]", "");
            String lowerLine = cleanLine.toLowerCase();

            if (lowerLine.contains("here are 10") || lowerLine.contains("brief and concise")){
                continue;
            }
            for (final Map.Entry<String, BiConsumer<String, String>> entry : recipeMap.entrySet()) {
                if (lowerLine.startsWith("recipe #")) {
                    int colonIndex = cleanLine.indexOf(":");
                    if (colonIndex != -1) {
                        title = cleanLine.substring(colonIndex + 1).trim();
                    }
                    continue;
                }
                if (lowerLine.contains(entry.getKey())){
                    entry.getValue().accept(cleanLine, lowerLine);
                    continue;
                }
                if (inIngredients && !cleanLine.isEmpty()){
                    if (!lowerLine.contains("prep time:") && !lowerLine.contains("recipe #")) {
                        ingredients.add("• " + cleanLine + "\n");
                    }
                }
            }

        }
    }
    private void processTitle(String cleanLine, String lowerLine) {
        title = cleanLine.substring(lowerLine.indexOf("title:") + 6).trim();
    }
    private void processIngredients(String cleanLine, String lowerLine){
        inIngredients = true;
        String restOfLine = cleanLine.substring(lowerLine.indexOf("ingredients:") + 12).trim();
        if (!restOfLine.isEmpty()) {
            ingredients.add("• " + restOfLine + "\n");
        }
    }
    private void processInstructions(String cleanLine, String lowerLine){
        inIngredients = false;
    }

    /**
     * Converts the recipe object to a formatted string compatible with the AI recipe format.
     * Trace Organ
     */
    public String toFormattedText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Title: ").append(title != null ? title : "Untitled Recipe").append("\n\n");
        
        sb.append("Ingredients:\n");
        if (ingredients != null) {
            for (String ingredient : ingredients) {
                sb.append("- ").append(ingredient).append("\n");
            }
        }
        sb.append("\n");
        
        sb.append("Instructions:\n");
        if (instructions != null) {
            for (int i = 0; i < instructions.size(); i++) {
                sb.append(i + 1).append(". ").append(instructions.get(i)).append("\n");
            }
        }
        
        return sb.toString();
    }
}
