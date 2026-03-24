package com.example.eecs4443project;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.common.util.concurrent.ListenableFuture;

public class gemeniAPI {
    // This pulls your key
    private static final String API_KEY = com.example.eecs4443project.BuildConfig.GEMENI_API_KEY;

    // Updated to exactly gemini-2.5-flash as requested
    public static final String MODEL_NAME = "gemini-2.5-flash";

    public static ListenableFuture<GenerateContentResponse> getRecipeFromAI(String ingredients, boolean isSpicy, boolean isVegetarian) {

        // 1. Configure the model settings
        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.temperature = 0.8f; // Lowered slightly to improve consistency
        configBuilder.topP = 0.95f;
        // The Java SDK might have internal limits or the model might struggle with 8192
        // Trying a slightly lower but still large limit that is more standard
        configBuilder.maxOutputTokens = 4096; 
        GenerationConfig config = configBuilder.build();

        // 2. Initialize the model
        GenerativeModel gm = new GenerativeModel(MODEL_NAME, API_KEY, config);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        // 3. Build the prompt for 10 recipes
        StringBuilder promptBuilder = new StringBuilder();
        // Added "BRIEF" and "CONCISE" to reduce token usage per recipe
        promptBuilder.append("Create EXACTLY 10 different, BRIEF and CONCISE recipes using these ingredients: ").append(ingredients).append(". ");

        if (isVegetarian) {
            promptBuilder.append("All recipes MUST be vegetarian. ");
        }
        if (isSpicy) {
            promptBuilder.append("All recipes MUST be spicy. ");
        }

        // Asking for concise steps to fit more recipes in the token limit
        // Updated to explicitly ask for measurements in brackets
        promptBuilder.append("For EACH recipe, follow this exact structure:\n")
                     .append("Title: [Name]\n")
                     .append("Prep Time: [Time]\n")
                     .append("Ingredients: [List each ingredient followed by its measurement in brackets, e.g., Flour (2 cups), Salt (1 tsp)]\n")
                     .append("Instructions: [Max 3 very short steps]\n")
                     .append("End each recipe with '---'.");

        // 4. Create the content object
        Content content = new Content.Builder()
                .addText(promptBuilder.toString())
                .build();

        // 5. Generate and return the future
        return model.generateContent(content);
    }
}
