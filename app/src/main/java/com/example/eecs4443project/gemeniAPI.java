package com.example.eecs4443project;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.common.util.concurrent.ListenableFuture;

public class gemeniAPI {
    // This pulls your key from secrets.properties
    private static final String API_KEY = com.example.eecs4443project.BuildConfig.GEMENI_API_KEY;

    // Standard model ID for the 2.5 Flash model
    public static final String MODEL_NAME = "gemini-2.5-flash";

    public static ListenableFuture<GenerateContentResponse> getRecipeFromAI(String ingredients, boolean isSpicy, boolean isVegetarian) {

        // 1. Configure the model settings
        // Note: Using field access as the Java SDK Builder uses public fields
        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.temperature = 0.75f;
        configBuilder.topP = 0.95f;
        GenerationConfig config = configBuilder.build();

        // 2. Initialize the model
        GenerativeModel gm = new GenerativeModel(MODEL_NAME, API_KEY, config);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        // 3. Build the prompt based on checkboxes
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Create a delicious recipe using these ingredients: ").append(ingredients).append(". ");

        if (isVegetarian) {
            promptBuilder.append("The recipe MUST be vegetarian. ");
        }
        if (isSpicy) {
            promptBuilder.append("The recipe MUST be spicy. ");
        }

        promptBuilder.append("Format the response clearly with a 'Title:', 'Prep Time:', 'Ingredients List:', and 'Step-by-Step Instructions:'.");

        // 4. Create the content object
        Content content = new Content.Builder()
                .addText(promptBuilder.toString())
                .build();

        // 5. Generate and return the future
        return model.generateContent(content);
    }
}