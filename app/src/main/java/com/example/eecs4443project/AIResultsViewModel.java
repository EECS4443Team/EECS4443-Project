package com.example.eecs4443project;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.List;

/**
 * ViewModel for the AI Results screen.
 * Calls the Gemini AI to generate recipes and exposes the results as LiveData.
 * Survives screen rotation so the API is not called again unnecessarily.
 */
public class AIResultsViewModel extends ViewModel {
    private static final String TAG = "AIResultsViewModel";

    private final MutableLiveData<List<RecipeParser.RecipeSection>> recipeSections = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Stores the raw AI response so we don't re-fetch after rotation
    private String savedAiResponse = "";

    public LiveData<List<RecipeParser.RecipeSection>> getRecipeSections() {
        return recipeSections;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Fetches recipes from the Gemini AI.
     * If recipes were already fetched (e.g. before a screen rotation), re-uses the saved response.
     */
    public void fetchRecipes(String ingredients, boolean isSpicy, boolean isVegetarian) {
        // If we already have a response, just re-parse it instead of calling the API again
        if (!savedAiResponse.isEmpty()) {
            recipeSections.setValue(RecipeParser.parseAIResponse(savedAiResponse));
            return;
        }

        isLoading.setValue(true);

        Futures.addCallback(
                GeminiApiHelper.generateRecipes(ingredients, isSpicy, isVegetarian),
                new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        savedAiResponse = result.getText();
                        Log.d(TAG, "AI response received. Length: "
                                + (savedAiResponse != null ? savedAiResponse.length() : 0));

                        // Handle empty or null response
                        if (savedAiResponse == null || savedAiResponse.trim().isEmpty()) {
                            savedAiResponse = "";
                            errorMessage.postValue("No recipes were returned. Please try again.");
                            isLoading.postValue(false);
                            return;
                        }

                        // Parse the response into individual recipe sections
                        List<RecipeParser.RecipeSection> parsedSections =
                                RecipeParser.parseAIResponse(savedAiResponse);

                        if (parsedSections.isEmpty()) {
                            errorMessage.postValue("Could not parse recipes. Please try again.");
                        }

                        recipeSections.postValue(parsedSections);
                        isLoading.postValue(false);
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        Log.e(TAG, "AI request failed: " + error.getMessage());
                        errorMessage.postValue("Error: " + error.getMessage());
                        isLoading.postValue(false);
                    }
                },
                MoreExecutors.directExecutor()
        );
    }
}
