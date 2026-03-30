package com.example.eecs4443project;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RecipeDetailActivityTest {

    @Test
    public void testRecipeDetailDisplayAndNavigation() {
        // Mock Intent with Recipe Data
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, RecipeDetailActivity.class);
        String mockRecipe = "Title: Chocolate Cake\nIngredients:\n1. Flour\n2. Sugar\nInstructions:\n1. Mix all.";
        intent.putExtra("recipe_text", mockRecipe);

        // Launch Activity
        ActivityScenario.launch(intent);

        // Verify Title and Ingredients are shown
        onView(withId(R.id.recipeTitle)).check(matches(withText("Chocolate Cake")));
        onView(withId(R.id.recipeIngredients)).check(matches(isDisplayed()));

        // Toggle to Card mode
        onView(withId(R.id.toggleCard)).perform(click());

        // Verify Start Cooking button is clickable
        onView(withId(R.id.startCookingButton)).check(matches(isDisplayed()));
        onView(withId(R.id.saveRecipeButton)).check(matches(isDisplayed()));
    }
}
