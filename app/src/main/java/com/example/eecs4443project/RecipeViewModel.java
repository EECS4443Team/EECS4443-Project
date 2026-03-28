package com.example.eecs4443project;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class RecipeViewModel extends ViewModel {
    private final MutableLiveData<List<String>> steps = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> currentStepPosition = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isGridView = new MutableLiveData<>(false);
    private String rawRecipeText;

    public LiveData<List<String>> getSteps() {
        return steps;
    }

    public LiveData<Integer> getCurrentStepPosition() {
        return currentStepPosition;
    }

    public LiveData<Boolean> getIsGridView() {
        return isGridView;
    }

    public void setRawRecipeText(String text) {
        if (this.rawRecipeText != null && this.rawRecipeText.equals(text)) return;
        this.rawRecipeText = text;
        parseAndSetSteps(text);
    }

    public void setCurrentStepPosition(int position) {
        currentStepPosition.setValue(position);
    }

    public void setGridView(boolean isGrid) {
        isGridView.setValue(isGrid);
    }

    private void parseAndSetSteps(String text) {
        List<String> parsedSteps = new ArrayList<>();
        if (text == null) {
            steps.setValue(parsedSteps);
            return;
        }

        String[] lines = text.split("\n");
        boolean inInstructions = false;
        StringBuilder currentStep = new StringBuilder();

        for (String line : lines) {
            String cleanLine = line.trim().replaceAll("[\\*#]", "");
            String lower = cleanLine.toLowerCase();

            if (lower.contains("instructions:") || lower.contains("steps:") || lower.contains("directions:")) {
                inInstructions = true;
                continue;
            }

            if (inInstructions && !cleanLine.isEmpty()) {
                if (cleanLine.matches("^\\d+[\\.\\)].*")) {
                    if (currentStep.length() > 0) {
                        parsedSteps.add(currentStep.toString().trim());
                    }
                    currentStep = new StringBuilder(cleanLine.replaceAll("^\\d+[\\.\\)]\\s*", ""));
                } else {
                    if (currentStep.length() > 0) {
                        currentStep.append(" ");
                    }
                    currentStep.append(cleanLine);
                }
            }
        }

        if (currentStep.length() > 0) {
            parsedSteps.add(currentStep.toString().trim());
        }

        if (parsedSteps.isEmpty()) {
            parsedSteps.add(text);
        }

        steps.setValue(parsedSteps);
    }

    public void nextStep() {
        Integer current = currentStepPosition.getValue();
        List<String> currentSteps = steps.getValue();
        if (current != null && currentSteps != null && current < currentSteps.size() - 1) {
            currentStepPosition.setValue(current + 1);
        }
    }

    public void previousStep() {
        Integer current = currentStepPosition.getValue();
        if (current != null && current > 0) {
            currentStepPosition.setValue(current - 1);
        }
    }
}
