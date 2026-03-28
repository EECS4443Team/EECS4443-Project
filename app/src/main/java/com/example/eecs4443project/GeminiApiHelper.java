package com.example.eecs4443project;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Helper class for calling the Google Gemini AI API to generate recipes.
 * Uses the Gemini 2.5 Flash model to create 10 recipes based on given ingredients.
 */
public class GeminiApiHelper {

    private static final String API_KEY = BuildConfig.GEMENI_API_KEY;
    private static final String MODEL_NAME = "gemini-2.5-flash";

    /**
     * Sends a request to the Gemini AI to generate 10 recipes.
     *
     * @param ingredients    A comma-separated string of ingredients the user selected.
     * @param isSpicy        Whether all recipes should be spicy.
     * @param isVegetarian   Whether all recipes should be vegetarian.
     * @return A ListenableFuture that will contain the AI's response when ready.
     */
    public static ListenableFuture<GenerateContentResponse> generateRecipes(
            String ingredients, boolean isSpicy, boolean isVegetarian) {

        // Set up the AI model configuration
        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.temperature = 0.7f;
        configBuilder.topP = 0.95f;
        configBuilder.maxOutputTokens = 4096;
        GenerationConfig config = configBuilder.build();

        // Create the Gemini model
        GenerativeModel geminiModel = new GenerativeModel(MODEL_NAME, API_KEY, config);
        GenerativeModelFutures model = GenerativeModelFutures.from(geminiModel);

        // Build the prompt that tells the AI what to generate
        String prompt = buildPrompt(ingredients, isSpicy, isVegetarian);

        // Create the message content and send it to the AI
        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        return model.generateContent(content);
    }

    /**
     * Builds the prompt string that instructs the AI on how to format the recipes.
     */
    private static String buildPrompt(String ingredients, boolean isSpicy, boolean isVegetarian) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Create EXACTLY 10 different, BRIEF and CONCISE recipes. ");
        prompt.append("The recipes MUST be strictly based on these ingredients: ");
        prompt.append(ingredients).append(". ");
        prompt.append("Do NOT suggest recipes that rely on main ingredients not listed above. ");

        if (isVegetarian) {
            prompt.append("All recipes MUST be vegetarian. ");
        }
        if (isSpicy) {
            prompt.append("All recipes MUST be spicy. ");
        }

        prompt.append("For EACH recipe, follow this exact structure:\n");
        prompt.append("Title: [Name]\n");
        prompt.append("Prep Time: [Time]\n");
        prompt.append("Ingredients: [List each ingredient followed by its measurement ");
        prompt.append("in brackets, e.g., Flour (2 cups), Salt (1 tsp)]\n");
        prompt.append("Instructions: [Max 3 very short steps]\n");
        prompt.append("End each recipe with '---'.");

        return prompt.toString();
    }
}
