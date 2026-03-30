package com.example.eecs4443project;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RecipeStepSwipeActivityTest {

    @Test
    public void testNavigationBetweenSteps() {
        // Create an intent with mock recipe text
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, RecipeStepSwipeActivity.class);
        String mockRecipe = "Title: Test Recipe\nInstructions:\n1. First Step\n2. Second Step";
        intent.putExtra("recipe_text", mockRecipe);

        // Launch the activity
        ActivityScenario.launch(intent);

        // Check if the first step indicator is correct
        onView(withId(R.id.stepIndicator)).check(matches(withText("Step 1")));

        // Click Next button
        onView(withId(R.id.nextButton)).perform(click());

        // Check if the step indicator updated to Step 2
        onView(withId(R.id.stepIndicator)).check(matches(withText("Step 2")));

        // Click Previous button
        onView(withId(R.id.prevButton)).perform(click());

        // Check if it returned to Step 1
        onView(withId(R.id.stepIndicator)).check(matches(withText("Step 1")));
    }
}
