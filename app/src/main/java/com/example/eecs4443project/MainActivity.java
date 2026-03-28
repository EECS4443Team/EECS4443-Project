package com.example.eecs4443project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Main screen where the user selects ingredients, filters, and chooses
 * between AI generation or manual website search for recipes.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // Available ingredients the user can select from
    private final String[] AVAILABLE_INGREDIENTS = {
            "Chicken", "Beef", "Pork", "Shrimp", "Salmon",
            "Potato", "Spinach", "Mushroom", "Tomato", "Onion",
            "Pasta", "Rice", "Cheese", "Bacon", "Garlic"
    };

    private final List<CheckBox> ingredientCheckBoxes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find all the UI elements
        MaterialButtonToggleGroup modeToggle = findViewById(R.id.acquisitionToggle);
        EditText searchInput = findViewById(R.id.searchInput);
        CheckBox spicyCheckBox = findViewById(R.id.spicyCheck);
        CheckBox vegetarianCheckBox = findViewById(R.id.vegetarianCheck);
        LinearLayout ingredientsContainer = findViewById(R.id.ingredientsContainer);
        Button searchButton = findViewById(R.id.searchButton);
        Button viewSavedButton = findViewById(R.id.viewSavedButton);

        // Create checkboxes for each ingredient and add them to the container
        for (String ingredientName : AVAILABLE_INGREDIENTS) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(ingredientName);
            ingredientCheckBoxes.add(checkBox);
            ingredientsContainer.addView(checkBox);
        }

        // Default to manual search mode
        modeToggle.check(R.id.toggleManual);

        // Handle search button click
        searchButton.setOnClickListener(view -> {
            String searchText = searchInput.getText().toString().trim();
            boolean isAIMode = (modeToggle.getCheckedButtonId() == R.id.toggleAI);

            // Collect selected ingredients
            JSONArray selectedIngredients = new JSONArray();
            StringBuilder queryText = new StringBuilder();
            if (!searchText.isEmpty()) {
                queryText.append(searchText);
            }
            for (CheckBox checkBox : ingredientCheckBoxes) {
                if (checkBox.isChecked()) {
                    String ingredientName = checkBox.getText().toString();
                    selectedIngredients.put(ingredientName);
                    if (queryText.length() > 0) {
                        queryText.append(", ");
                    }
                    queryText.append(ingredientName);
                }
            }

            if (isAIMode) {
                openAIResultsScreen(queryText.toString(), spicyCheckBox.isChecked(),
                        vegetarianCheckBox.isChecked());
            } else {
                openWebSearchScreen(selectedIngredients);
            }
        });

        // Handle "View Saved Recipes" button click
        viewSavedButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, SavedRecipesActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Opens the AI Results screen to generate recipes using Gemini.
     */
    private void openAIResultsScreen(String query, boolean isSpicy, boolean isVegetarian) {
        Intent intent = new Intent(this, AIResultsActivity.class);
        intent.putExtra("query", query);
        intent.putExtra("spicy", isSpicy);
        intent.putExtra("vegetarian", isVegetarian);
        startActivity(intent);
    }

    /**
     * Opens a bottom sheet with a WebView to browse recipes on SpendWithPennies.
     * When the user clicks a recipe link, it parses that recipe and opens the detail screen.
     */
    private void openWebSearchScreen(JSONArray selectedIngredients) {
        // Build the search URL for the recipe website
        HtmlParser parser = new HtmlParser();
        String searchUrl = parser.buildSearchUrl(selectedIngredients);
        Log.d(TAG, "Search URL: " + searchUrl);

        // This array holds the URL of the recipe the user clicks on
        // (uses an array so it can be modified inside the lambda)
        final String[] clickedRecipeUrl = {""};

        // Create and show a bottom sheet dialog with a WebView
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        WebView webView = createRecipeWebView(clickedRecipeUrl, bottomSheet, searchUrl);

        // When the bottom sheet is closed, parse the selected recipe
        bottomSheet.setOnDismissListener(dialog -> {
            webView.stopLoading();

            if (clickedRecipeUrl[0] == null || clickedRecipeUrl[0].isEmpty()) {
                Log.d(TAG, "User closed without selecting a recipe");
                webView.destroy();
                return;
            }

            // Parse the recipe in a background thread (network calls can't run on main thread)
            new Thread(() -> {
                try {
                    HtmlParser recipeParser = new HtmlParser(clickedRecipeUrl[0]);
                    Recipe recipe = recipeParser.getRecipe();

                    runOnUiThread(() -> {
                        if (recipe.getTitle() != null) {
                            Intent intent = new Intent(this, RecipeDetailActivity.class);
                            intent.putExtra("recipe_text", recipe.toFormattedText());
                            intent.putExtra("acquisition_mode", "local");
                            webView.destroy();
                            startActivity(intent);
                        } else {
                            Log.e(TAG, "Recipe parsing failed: title is null");
                        }
                    });
                } catch (Exception error) {
                    Log.e(TAG, "Error parsing recipe: " + error.getMessage());
                }
            }).start();
        });

        // Show the bottom sheet expanded to full height
        bottomSheet.setContentView(webView);
        bottomSheet.show();
        View bottomSheetView = bottomSheet.findViewById(
                com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheetView != null) {
            bottomSheetView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            BottomSheetBehavior.from(bottomSheetView).setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    /**
     * Creates a WebView configured to browse the recipe website.
     * When the user clicks on a specific recipe link, it saves the URL and closes the dialog.
     */
    @SuppressLint("SetJavaScriptEnabled")
    @NonNull
    private WebView createRecipeWebView(String[] clickedRecipeUrl,
                                        BottomSheetDialog bottomSheet,
                                        String searchUrl) {
        WebView webView = new WebView(this);
        webView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        // Enable JavaScript for the recipe website to work properly
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String clickedUrl = request.getUrl().toString();

                // If the user clicked a link that's not the search page,
                // it's a recipe link — save it and close the browser
                if (!clickedUrl.equals(searchUrl)) {
                    clickedRecipeUrl[0] = clickedUrl;
                    Log.d(TAG, "User selected recipe URL: " + clickedUrl);
                    bottomSheet.dismiss();
                    return true;
                }
                return false;
            }
        });

        webView.loadUrl(searchUrl);
        return webView;
    }
}
