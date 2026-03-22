package com.example.eecs4443project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
    private final String[] INGREDIENTS = {"Chicken", "Beef", "Pork", "Shrimp", "Salmon", "Potato", "Spinach", "Mushroom","Tomato", "Onion", "Pasta", "Rice", "Cheese", "Bacon", "Garlic"};
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
        acquisitionToggle.check(R.id.toggleManual);

        searchButton.setOnClickListener(v -> {
            final String[] selectedRecipeUrl = {""};
            StringBuilder queryBuilder = new StringBuilder(searchInput.getText().toString().trim());
            JSONArray selectedIngredients = new JSONArray();
            for (CheckBox checkBox : checkBoxList) {
                if (checkBox.isChecked()) {
                    Log.d("MainActivity", "Selected ingredient: " + checkBox.getText().toString());
                    selectedIngredients.put(checkBox.getText().toString());
                }
            }

            for (String ingredient : INGREDIENTS) {
                if (queryBuilder.length() > 0) queryBuilder.append(", ");
                queryBuilder.append(ingredient);
            }

            boolean isAI = acquisitionToggle.getCheckedButtonId() == R.id.toggleAI;
            String acquisitionMode = isAI ? "ai" : "local";

            // [Step 1] HtmlParser Search Triggered
            Log.d("MainActivity", "--- [Step 1] HtmlParser Search Triggered ---");
            Log.d("MainActivity", "Ingredients: " + selectedIngredients);

            HtmlParser parser = new HtmlParser();
            String searchUrl = parser.buildSearchUrl(selectedIngredients);
            Log.d("MainActivity", "Search URL Generated: " + searchUrl);

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

                //If the Dialog is dismissed(closed), move to SearchResultsActivity
                bottomSheetDialog.setOnDismissListener(dialog -> {
                    webView.stopLoading();
                    webView.destroy();
                    HtmlParser htmlParser = new HtmlParser(selectedRecipeUrl[0]);
                    Recipe recipe = htmlParser.getRecipe();

                    Intent intent = new Intent(this, RecipeDetailActivity.class);
                    intent.putExtra("recipe", recipe);
                    intent.putExtra("acquisition_mode", acquisitionMode);
                    //To retrieve, Recipe user = (Recipe) getIntent().getSerializableExtra("recipe");
                    startActivity(intent);
                });

                bottomSheetDialog.setContentView(webView);
                bottomSheetDialog.show();
            }
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
                if (url.startsWith("https://www.spendwithpennies.com/")) {
                    selectedRecipeUrl[0] = url; // save the url
                    Log.d("MainActivity", "Selected Recipe URL: " + selectedRecipeUrl[0]);
                    bottomSheetDialog.dismiss(); // cloase the webview
                    return true; //To prevent loading the url in the webview, we need to move to recipe detail activity.
                }
                return false;
            }
        });

        webView.loadUrl(searchUrl);
        return webView;
    }
}