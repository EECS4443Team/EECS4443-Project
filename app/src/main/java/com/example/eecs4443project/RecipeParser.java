package com.example.eecs4443project;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that handles all recipe text parsing.
 * Recipes from the AI and from websites follow a text format with sections
 * like "Title:", "Ingredients:", and "Instructions:". This class extracts
 * the data from that format.
 */
public class RecipeParser {

    /**
     * Holds parsed recipe data (title, ingredients, instructions) ready for database storage.
     */
    public static class ParsedRecipe {
        public final String title;
        public final String ingredients;
        public final String instructions;

        public ParsedRecipe(String title, String ingredients, String instructions) {
            this.title = title;
            this.ingredients = ingredients;
            this.instructions = instructions;
        }
    }

    /**
     * Represents one recipe section from the AI response (title + full text).
     */
    public static class RecipeSection {
        public final String title;
        public final String fullText;

        public RecipeSection(String title, String fullText) {
            this.title = title;
            this.fullText = fullText;
        }
    }

    // -----------------------------------------------------------------------
    // Helper: removes markdown bold (**) and header (#) markers from a line
    // -----------------------------------------------------------------------

    private static String removeMarkdownMarkers(String line) {
        return line.trim().replaceAll("[\\*#]", "");
    }

    // -----------------------------------------------------------------------
    // Parsing methods
    // -----------------------------------------------------------------------

    /**
     * Extracts the recipe title from recipe text.
     * Looks for lines containing "Title:" or starting with "Recipe #".
     */
    public static String parseTitle(String text) {
        if (text == null) return "AI Generated Recipe";

        String[] lines = text.split("\n");
        for (String line : lines) {
            String cleanLine = removeMarkdownMarkers(line);
            String lowerCaseLine = cleanLine.toLowerCase();

            if (lowerCaseLine.contains("title:")) {
                int startIndex = lowerCaseLine.indexOf("title:") + 6;
                return cleanLine.substring(startIndex).trim();
            } else if (lowerCaseLine.startsWith("recipe #")) {
                int colonIndex = cleanLine.indexOf(":");
                if (colonIndex != -1) {
                    return cleanLine.substring(colonIndex + 1).trim();
                }
            }
        }
        return "AI Generated Recipe";
    }

    /**
     * Parses ingredients for the detail screen, adding bullet points for display.
     * Only extracts lines between "Ingredients:" and "Instructions:" sections.
     */
    public static String parseIngredientsForDisplay(String text) {
        if (text == null) return "";

        String[] lines = text.split("\n");
        StringBuilder ingredientsList = new StringBuilder();
        boolean insideIngredientsSection = false;

        for (String line : lines) {
            String cleanLine = removeMarkdownMarkers(line);
            String lowerCaseLine = cleanLine.toLowerCase();

            // Skip AI preamble lines
            if (lowerCaseLine.contains("here are 10") || lowerCaseLine.contains("brief and concise")) {
                continue;
            }

            if (lowerCaseLine.contains("ingredients:")) {
                insideIngredientsSection = true;
                // Check if there's ingredient text on the same line after "Ingredients:"
                String restOfLine = cleanLine.substring(lowerCaseLine.indexOf("ingredients:") + 12).trim();
                if (!restOfLine.isEmpty()) {
                    ingredientsList.append("• ").append(restOfLine).append("\n");
                }
            } else if (lowerCaseLine.contains("instructions:")) {
                insideIngredientsSection = false;
            } else if (insideIngredientsSection && !cleanLine.isEmpty()) {
                // Skip metadata lines that might appear in the ingredients section
                if (!lowerCaseLine.contains("prep time:") && !lowerCaseLine.contains("recipe #")) {
                    ingredientsList.append("• ").append(cleanLine).append("\n");
                }
            }
        }

        return ingredientsList.toString().trim();
    }

    /**
     * Creates a simple fallback display when ingredient parsing fails.
     * Just cleans up the raw text and removes AI preamble.
     */
    public static String buildFallbackDisplay(String text) {
        if (text == null) return "";

        String[] lines = text.split("\n");
        StringBuilder fallbackText = new StringBuilder();

        for (String line : lines) {
            String cleanLine = line.trim();
            if (!cleanLine.toLowerCase().contains("here are 10") && !cleanLine.isEmpty()) {
                fallbackText.append(cleanLine).append("\n");
            }
        }

        return fallbackText.toString().trim();
    }

    /**
     * Parses recipe text and extracts title, ingredients, and instructions
     * as plain strings, ready to be saved to the database.
     */
    public static ParsedRecipe parseForDatabase(String text) {
        if (text == null) return new ParsedRecipe("AI Generated Recipe", "", "");

        String title = "AI Generated Recipe";
        StringBuilder ingredients = new StringBuilder();
        StringBuilder instructions = new StringBuilder();

        String[] lines = text.split("\n");
        boolean insideIngredientsSection = false;
        boolean insideInstructionsSection = false;

        for (String line : lines) {
            String cleanLine = removeMarkdownMarkers(line);
            String lowerCaseLine = cleanLine.toLowerCase();

            if (lowerCaseLine.contains("title:")) {
                // Extract the title
                int startIndex = lowerCaseLine.indexOf("title:") + 6;
                title = cleanLine.substring(startIndex).trim();

            } else if (lowerCaseLine.startsWith("recipe #")) {
                // Alternative title format: "Recipe #1: Chicken Stir Fry"
                int colonIndex = cleanLine.indexOf(":");
                if (colonIndex != -1) {
                    title = cleanLine.substring(colonIndex + 1).trim();
                }

            } else if (lowerCaseLine.contains("ingredients:")) {
                // Start of ingredients section
                insideIngredientsSection = true;
                insideInstructionsSection = false;
                String restOfLine = cleanLine.substring(lowerCaseLine.indexOf("ingredients:") + 12).trim();
                if (!restOfLine.isEmpty()) {
                    ingredients.append(restOfLine).append("\n");
                }

            } else if (lowerCaseLine.contains("instructions:")
                    || lowerCaseLine.contains("steps:")
                    || lowerCaseLine.contains("directions:")) {
                // Start of instructions section
                insideIngredientsSection = false;
                insideInstructionsSection = true;
                String restOfLine = cleanLine.substring(cleanLine.indexOf(":") + 1).trim();
                if (!restOfLine.isEmpty()) {
                    instructions.append(restOfLine).append("\n");
                }

            } else if (insideIngredientsSection && !cleanLine.isEmpty()) {
                ingredients.append(cleanLine).append("\n");

            } else if (insideInstructionsSection && !cleanLine.isEmpty()) {
                instructions.append(cleanLine).append("\n");
            }
        }

        return new ParsedRecipe(
                title,
                ingredients.toString().trim(),
                instructions.toString().trim()
        );
    }

    /**
     * Parses ingredients with their amounts for the cooking step view.
     * Adds bullet points to each ingredient line.
     */
    public static String parseIngredientsWithAmounts(String text) {
        if (text == null) return "";

        String[] lines = text.split("\n");
        StringBuilder ingredientsList = new StringBuilder();
        boolean insideIngredientsSection = false;

        for (String line : lines) {
            String cleanLine = removeMarkdownMarkers(line);
            String lowerCaseLine = cleanLine.toLowerCase();

            if (lowerCaseLine.contains("ingredients")) {
                insideIngredientsSection = true;
                // Check if there's text after the colon on the same line
                int colonIndex = cleanLine.indexOf(":");
                if (colonIndex != -1 && colonIndex < cleanLine.length() - 1) {
                    String restOfLine = cleanLine.substring(colonIndex + 1).trim();
                    if (!restOfLine.isEmpty()) {
                        ingredientsList.append("• ").append(restOfLine).append("\n");
                    }
                }
                continue;
            }

            // Stop at instructions/steps/directions section
            if (lowerCaseLine.contains("instructions")
                    || lowerCaseLine.contains("steps")
                    || lowerCaseLine.contains("directions")) {
                insideIngredientsSection = false;
                continue;
            }

            if (insideIngredientsSection && !cleanLine.isEmpty()) {
                // Add bullet point if the line doesn't already have one
                if (!cleanLine.startsWith("•") && !cleanLine.startsWith("-")) {
                    ingredientsList.append("• ").append(cleanLine).append("\n");
                } else {
                    ingredientsList.append(cleanLine).append("\n");
                }
            }
        }

        return ingredientsList.toString().trim();
    }

    /**
     * Parses cooking steps from recipe text.
     * Each numbered step (e.g., "1. Preheat oven") becomes a separate item in the list.
     */
    public static List<String> parseSteps(String text) {
        List<String> stepsList = new ArrayList<>();
        if (text == null) return stepsList;

        String[] lines = text.split("\n");
        boolean insideInstructionsSection = false;
        StringBuilder currentStep = new StringBuilder();

        for (String line : lines) {
            String cleanLine = removeMarkdownMarkers(line);
            String lowerCaseLine = cleanLine.toLowerCase();

            // Detect the start of the instructions section
            if (lowerCaseLine.contains("instructions")
                    || lowerCaseLine.contains("steps")
                    || lowerCaseLine.contains("directions")) {
                insideInstructionsSection = true;
                // Check if there's step text on the same line after the colon
                int colonIndex = cleanLine.indexOf(":");
                if (colonIndex != -1 && colonIndex < cleanLine.length() - 1) {
                    String restOfLine = cleanLine.substring(colonIndex + 1).trim();
                    if (!restOfLine.isEmpty()) {
                        currentStep.append(restOfLine);
                    }
                }
                continue;
            }

            // Detect the end of instructions (another section starts)
            if (insideInstructionsSection
                    && (lowerCaseLine.contains("ingredients")
                    || lowerCaseLine.contains("notes")
                    || lowerCaseLine.contains("prep time"))) {
                if (currentStep.length() > 0) {
                    stepsList.add(currentStep.toString().trim());
                    currentStep.setLength(0);
                }
                insideInstructionsSection = false;
                continue;
            }

            // Process instruction lines
            if (insideInstructionsSection && !cleanLine.isEmpty()) {
                boolean startsWithNumber = cleanLine.matches("^\\d+[\\.\\)].*");

                if (startsWithNumber) {
                    // Save the previous step and start a new one
                    if (currentStep.length() > 0) {
                        stepsList.add(currentStep.toString().trim());
                        currentStep.setLength(0);
                    }
                    // Remove the number prefix (e.g., "1. " or "2) ")
                    String stepText = cleanLine.replaceAll("^\\d+[\\.\\)]\\s*", "");
                    currentStep.append(stepText);
                } else {
                    // Continue the current step (multi-line step text)
                    if (currentStep.length() > 0) {
                        currentStep.append(" ");
                    }
                    currentStep.append(cleanLine);
                }
            }
        }

        // Don't forget the last step
        if (currentStep.length() > 0) {
            stepsList.add(currentStep.toString().trim());
        }

        return stepsList;
    }

    /**
     * Splits an AI response into individual recipe sections.
     * The AI is asked to separate recipes with "---", but sometimes uses
     * other separators like "***" or "___", or no separator at all.
     * This method tries multiple splitting strategies.
     */
    public static List<RecipeSection> parseAIResponse(String response) {
        List<RecipeSection> sectionsList = new ArrayList<>();
        if (response == null || response.trim().isEmpty()) return sectionsList;

        // Strategy 1: Split by common separators (---, ***, ___)
        String[] rawSections = response.split("\\s*(-{3,}|\\*{3,}|_{3,})\\s*");

        // Strategy 2: If that didn't work, try splitting by "Recipe #" headers
        if (rawSections.length <= 1) {
            rawSections = response.split("(?=(?:Recipe\\s*#|\\*\\*Recipe\\s*#))");
        }

        // Strategy 3: Try splitting by "Title:" appearing at the start of a line
        if (rawSections.length <= 1) {
            rawSections = response.split("(?m)(?=^\\s*\\**\\s*Title:)");
        }

        // Process each section to extract its title
        for (String section : rawSections) {
            String trimmedSection = section.trim();

            // Skip sections that are too short to be a real recipe
            if (trimmedSection.length() < 10) continue;

            String title = extractTitleFromSection(trimmedSection);
            sectionsList.add(new RecipeSection(title, trimmedSection));
        }

        return sectionsList;
    }

    /**
     * Extracts the title from a single recipe section.
     * First looks for a "Title:" line, then falls back to the first non-empty line.
     */
    private static String extractTitleFromSection(String sectionText) {
        String[] lines = sectionText.split("\n");

        // Look for a line containing "Title:"
        for (String line : lines) {
            String cleanLine = removeMarkdownMarkers(line);
            if (cleanLine.toLowerCase().contains("title:")) {
                int startIndex = cleanLine.toLowerCase().indexOf("title:") + 6;
                return cleanLine.substring(startIndex).trim();
            }
        }

        // Fallback: use the first non-empty line as the title
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                return removeMarkdownMarkers(line);
            }
        }

        return "New Recipe";
    }
}
