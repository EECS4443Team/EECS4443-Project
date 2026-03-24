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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final String[] INGREDIENTS = {"Chicken", "Beef", "Pork", "Shrimp", "Salmon", "Potato", "Spinach", "Mushroom", "Tomato", "Onion", "Pasta", "Rice", "Cheese", "Bacon", "Garlic"};
    private final List<CheckBox> checkBoxList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialButtonToggleGroup acquisitionToggle = findViewById(R.id.acquisitionToggle);
        EditText searchInput = findViewById(R.id.searchInput);
        CheckBox spicyCheck = findViewById(R.id.spicyCheck);
        CheckBox vegetarianCheck = findViewById(R.id.vegetarianCheck);

        LinearLayout container = findViewById(R.id.ingredientsContainer);

        for (String ingredient : INGREDIENTS) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(ingredient);
            checkBox.setTag(ingredient);
            checkBoxList.add(checkBox);
            container.addView(checkBox);
        }

        Button searchButton = findViewById(R.id.searchButton);
        Button viewSavedButton = findViewById(R.id.viewSavedButton);

        acquisitionToggle.check(R.id.toggleManual);

        searchButton.setOnClickListener(v -> {
            final String[] selectedRecipeUrl = {""};
            StringBuilder queryBuilder = new StringBuilder(searchInput.getText().toString().trim());
            JSONArray selectedIngredients = new JSONArray();
            for (CheckBox checkBox : checkBoxList) {
                if (checkBox.isChecked()) {
                    Log.d("Recipe", "Selected ingredient: " + checkBox.getText().toString());
                    selectedIngredients.put(checkBox.getText().toString());
                }
            }
            if (!searchInput.getText().toString().isEmpty())
                queryBuilder.append(searchInput.getText().toString()).append(", ");

            for (String ingredient : INGREDIENTS) {
                if (queryBuilder.length() > 0) queryBuilder.append(", ");
                queryBuilder.append(ingredient);
            }

            boolean isAI = acquisitionToggle.getCheckedButtonId() == R.id.toggleAI;
            String acquisitionMode = isAI ? "ai" : "local";

            // HtmlParser Search Triggered
            Log.d("Recipe", "--- HtmlParser Search Triggered ---");
            Log.d("Recipe", "Ingredients: " + selectedIngredients);

            HtmlParser parser = new HtmlParser();
            String searchUrl = parser.buildSearchUrl(selectedIngredients);
            Log.d("Recipe", "Search URL Generated: " + searchUrl);

            if (acquisitionMode.equals("ai")) {
                // AI Mode
                Intent intent = new Intent(this, AIResultsActivity.class);
                intent.putExtra("selected_ingredients", selectedIngredients.toString());
                intent.putExtra("query", queryBuilder.toString());
                intent.putExtra("acquisition_mode", acquisitionMode);
                intent.putExtra("spicy", spicyCheck.isChecked());
                intent.putExtra("vegetarian", vegetarianCheck.isChecked());
                startActivity(intent);
            } else {

                // WebView and BottomSheet settings
                com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog =
                        new com.google.android.material.bottomsheet.BottomSheetDialog(this);

                WebView webView = getWebView(selectedRecipeUrl, bottomSheetDialog, searchUrl);

                //If the Dialog is dismissed(closed), move to RecipeDetailActivity
                bottomSheetDialog.setOnDismissListener(dialog -> {
                    webView.stopLoading();
                    if (selectedRecipeUrl[0] == null || selectedRecipeUrl[0].isEmpty()) {
                        Log.d("Recipe", "The user closed without selecting recipe");
                        webView.destroy();
                        return;
                    }
                    new Thread(() -> {
                        try {

                            HtmlParser htmlParser = new HtmlParser(selectedRecipeUrl[0]);
                            Recipe recipe = htmlParser.getRecipe();
                            Log.d("recipe_debug", "Parsed Title: " + recipe.getTitle());
                            runOnUiThread(() -> {
                                if (recipe.getTitle() != null) {
                                    // Move to RecipeDetailActivity passing the formatted recipe text
                                    Intent intent = new Intent(this, RecipeDetailActivity.class);
                                    intent.putExtra("recipe_text", recipe.toFormattedText());
                                    // Use "ai" mode to reuse the text parsing logic in RecipeDetailActivity
                                    intent.putExtra("acquisition_mode", "local");
                                    Log.d("recipe_debug", "Title: " + recipe.title);

                                    webView.destroy();
                                    startActivity(intent);
                                } else {
                                    Log.e("recipe_debug", "Parsing failed: Title is null");
                                }
                            });
                        } catch (Exception e) {
                            Log.e("recipe_debug", "Error in Thread: " + e.getMessage());
                        }
                    }).start();


                });

                bottomSheetDialog.setContentView(webView);
                bottomSheetDialog.show();
                View bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                    com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet)
                            .setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });

        viewSavedButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SavedRecipesActivity.class);
            startActivity(intent);
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    @NonNull
    private WebView getWebView(String[] selectedRecipeUrl, BottomSheetDialog bottomSheetDialog, String searchUrl) {
        WebView webView = new WebView(this);
        webView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        android.webkit.WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                // Check whether the clicked url is not in SpendWithPennies's domain
                if (!url.equals(searchUrl)) {
                    selectedRecipeUrl[0] = url; // save the url
                    Log.d("Recipe", "Selected Recipe URL: " + selectedRecipeUrl[0]);
                    bottomSheetDialog.dismiss(); // close the webview
                    return true; //To prevent loading the url in the webview, we need to move to recipe detail activity.
                }
                return false;
            }
        });

        webView.loadUrl(searchUrl);
        return webView;
    }
}
