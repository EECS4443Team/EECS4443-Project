package com.example.eecs4443project;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * A class responsible for parsing recipe data (title, ingredients, and instructions) from a given URL.
 * It extracts metadata from the HTML source, specifically targeting application/ld+json formats.
 */
public class HtmlParser {
    private String url;
    private String title;
    private String imageUrl;
    private List<String> ingredients = new ArrayList<>();
    private final List<String> recipeInstructions = new ArrayList<>();
    private static final String TAG = "HtmlParser";

    public HtmlParser() {}

    public HtmlParser(String url) {
        this.url = url;
        parseDynamicRecipe();
    }

    /**
     * This would create search url in the recipe site for the given ingredients.
     * This would be used to load webview to get the search result page.
     * @return searchUrl including ingredients
     */
    public String buildSearchUrl(JSONArray selectedIngredients) {
        try {
            StringBuilder queryBuilder = new StringBuilder();
            for (int i = 0; i < selectedIngredients.length(); i++) {
                if (queryBuilder.length() > 0) {
                    queryBuilder.append(",");
                }
                queryBuilder.append(selectedIngredients.getString(i));
            }

            String encodedQuery = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                encodedQuery = URLEncoder.encode(queryBuilder.toString(), StandardCharsets.UTF_8)
                        .replace("+", "%20");
            }

            return "https://www.spendwithpennies.com/#search/q=" + encodedQuery;
        } catch (Exception e) {
            Log.e(TAG, "Error building search URL: " + e.getMessage());
            return "https://www.spendwithpennies.com/";
        }
    }

    /**
     * Return the Recipe
     */
    public Recipe getRecipe() {
        return new Recipe(this.title, this.imageUrl, this.ingredients, this.recipeInstructions);
    }

    @NonNull
    @Override
    public String toString() {
        return "HtmlParser{" +
                "title='" + title + '\'' +
                ", ingredients=" + ingredients +
                ", instructions=" + recipeInstructions +
                '}';
    }

    /**
     * Dynamic Recipe Parsing
     */
    private void parseDynamicRecipe() {
        if (url == null || url.isEmpty()) return;
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36")
                    .get();
            Elements allImages = doc.select("img");

            // Check number of images, if more than 1, select 2nd image
            if (allImages.size() >= 2) {
                Element secondImg = allImages.get(1);

                // Chack data lazy src, if cannot find src
                if (secondImg.hasAttr("data-lazy-src")) {
                    this.imageUrl = secondImg.attr("data-lazy-src");
                } else if (secondImg.hasAttr("src")) {
                    // abs:src를 쓰면 상대 경로(예: /wp-content/...)를 절대 경로(예: https://...)로 바꿔줍니다.
                    this.imageUrl = secondImg.attr("abs:src");
                }

                Log.d(TAG, "Successfully parsed Second Image URL: " + this.imageUrl);
            } else {
                Log.w(TAG, "Less than 2 images found on this page.");
            }
            Elements scripts = doc.select("script[type=application/ld+json]");

            for (Element script : scripts) {
                String content = script.data().trim();
                if (content.isEmpty()) continue;

                JSONArray jsonArray;
                if (content.startsWith("{")) {
                    JSONObject obj = new JSONObject(content);
                    if (obj.has("@graph")) {
                        jsonArray = obj.getJSONArray("@graph");
                    } else {
                        jsonArray = new JSONArray().put(obj);
                    }
                } else {
                    jsonArray = new JSONArray(content);
                }
                //the recipe is contained in @type = Recipe
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject item = jsonArray.getJSONObject(i);
                    Object type = item.opt("@type");

                    if (isRecipeType(type)) {
                        processRecipe(item);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing recipe: " + e.getMessage());
        }
    }

    private boolean isRecipeType(Object type) {
        if (type instanceof String) return type.equals("Recipe");
        if (type instanceof JSONArray) {
            JSONArray types = (JSONArray) type;
            for (int i = 0; i < types.length(); i++) {
                if (types.optString(i).equals("Recipe")) return true;
            }
        }
        return false;
    }

    /**
     * Process the recipe
     * @throws JSONException
     * This is the entry point to extract ingredients and instructions from the recipe.
     */
    private void processRecipe(JSONObject recipe) throws JSONException {
        this.title = recipe.optString("name");

        if (recipe.has("recipeIngredient")) {
            JSONArray ingredientsArray = recipe.getJSONArray("recipeIngredient");
            for (int i = 0; i < ingredientsArray.length(); i++) {
                this.ingredients.add(ingredientsArray.getString(i));
            }
        }

        if (recipe.has("recipeInstructions")) {
            Object instructions = recipe.get("recipeInstructions");
            extractSteps(instructions);
        }

        if (recipe.has("image")) {
            Object imageObj = recipe.get("image");
            if (imageObj instanceof  JSONArray){
                this.imageUrl = ((JSONArray) imageObj).getString(0);
            }
        }

    }

    private void extractSteps(Object obj) throws JSONException {
        if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray) obj;
            for (int i = 0; i < array.length(); i++) {
                extractSteps(array.get(i));
            }
        } else if (obj instanceof JSONObject) {
            JSONObject node = (JSONObject) obj;
            String type = node.optString("@type");

            if ("HowToStep".equals(type)) {
                String stepText = node.optString("text");
                recipeInstructions.add(stepText);
            } else if ("HowToSection".equals(type) || node.has("itemListElement")) {
                extractSteps(node.get("itemListElement"));
            } else if (node.has("text")) {
                recipeInstructions.add(node.getString("text"));
            }
        } else if (obj instanceof String) {
            recipeInstructions.add((String) obj);
        }
    }
}
