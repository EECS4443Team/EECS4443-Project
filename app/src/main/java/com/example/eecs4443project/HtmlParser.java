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
 * Parses recipe data (title, ingredients, instructions, image) from a recipe website URL.
 * Uses Jsoup to download the HTML and extracts structured data from the JSON-LD metadata
 * that most recipe websites embed in their pages.
 */
public class HtmlParser {
    private static final String TAG = "HtmlParser";

    private String url;
    private String title;
    private String imageUrl;
    private final List<String> ingredientsList = new ArrayList<>();
    private final List<String> instructionsList = new ArrayList<>();

    /**
     * Default constructor for building search URLs without parsing.
     */
    public HtmlParser() {}

    /**
     * Creates an HtmlParser and immediately parses the recipe at the given URL.
     */
    public HtmlParser(String url) {
        this.url = url;
        parseRecipeFromWebsite();
    }

    /**
     * Builds a search URL for the SpendWithPennies recipe website
     * using the selected ingredients as the search query.
     *
     * @param selectedIngredients A JSONArray of ingredient names.
     * @return The search URL string.
     */
    public String buildSearchUrl(JSONArray selectedIngredients) {
        try {
            // Combine all ingredients into a comma-separated string
            StringBuilder queryBuilder = new StringBuilder();
            for (int i = 0; i < selectedIngredients.length(); i++) {
                if (queryBuilder.length() > 0) {
                    queryBuilder.append(",");
                }
                queryBuilder.append(selectedIngredients.getString(i));
            }

            // Encode the query for use in a URL
            String encodedQuery = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                encodedQuery = URLEncoder.encode(queryBuilder.toString(), StandardCharsets.UTF_8)
                        .replace("+", "%20");
            }

            return "https://www.spendwithpennies.com/#search/q=" + encodedQuery;
        } catch (Exception error) {
            Log.e(TAG, "Error building search URL: " + error.getMessage());
            return "https://www.spendwithpennies.com/";
        }
    }

    /**
     * Returns the parsed data as a Recipe object.
     */
    public Recipe getRecipe() {
        return new Recipe(this.title, this.ingredientsList, this.instructionsList);
    }

    @NonNull
    @Override
    public String toString() {
        return "HtmlParser{"
                + "title='" + title + '\''
                + ", ingredients=" + ingredientsList
                + ", instructions=" + instructionsList
                + '}';
    }

    /**
     * Downloads the HTML from the URL and extracts recipe data from JSON-LD metadata.
     * Most recipe websites embed structured data in <script type="application/ld+json"> tags.
     */
    private void parseRecipeFromWebsite() {
        if (url == null || url.isEmpty()) return;

        try {
            // Download the HTML page
            Document htmlDocument = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Linux; Android 13; Pixel 7) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/116.0.0.0 Mobile Safari/537.36")
                    .get();

            // Try to extract the recipe image from the page
            extractImageFromPage(htmlDocument);

            // Find and parse all JSON-LD script tags
            Elements jsonScripts = htmlDocument.select("script[type=application/ld+json]");
            for (Element script : jsonScripts) {
                String scriptContent = script.data().trim();
                if (scriptContent.isEmpty()) continue;

                // Parse the JSON content (could be an object or array)
                JSONArray jsonArray = parseJsonContent(scriptContent);

                // Look for a Recipe type in the JSON data
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject item = jsonArray.getJSONObject(i);
                    Object type = item.opt("@type");

                    if (isRecipeType(type)) {
                        extractRecipeData(item);
                    }
                }
            }
        } catch (Exception error) {
            Log.e(TAG, "Error parsing recipe from website: " + error.getMessage());
        }
    }

    /**
     * Tries to extract a recipe image from the HTML page.
     * Uses the second image on the page (the first is usually a logo).
     */
    private void extractImageFromPage(Document htmlDocument) {
        Elements allImages = htmlDocument.select("img");

        if (allImages.size() >= 2) {
            Element secondImage = allImages.get(1);

            // Some sites use lazy loading, so check data-lazy-src first
            if (secondImage.hasAttr("data-lazy-src")) {
                this.imageUrl = secondImage.attr("data-lazy-src");
            } else if (secondImage.hasAttr("src")) {
                // abs:src converts relative paths to absolute URLs
                this.imageUrl = secondImage.attr("abs:src");
            }

            Log.d(TAG, "Parsed image URL: " + this.imageUrl);
        } else {
            Log.w(TAG, "Less than 2 images found on this page.");
        }
    }

    /**
     * Parses a JSON string into a JSONArray, handling both object and array formats.
     */
    private JSONArray parseJsonContent(String jsonString) throws JSONException {
        if (jsonString.startsWith("{")) {
            JSONObject jsonObject = new JSONObject(jsonString);
            if (jsonObject.has("@graph")) {
                return jsonObject.getJSONArray("@graph");
            } else {
                return new JSONArray().put(jsonObject);
            }
        } else {
            return new JSONArray(jsonString);
        }
    }

    /**
     * Checks if a JSON-LD @type value represents a Recipe.
     * The type can be a string ("Recipe") or an array (["Recipe", "..."]).
     */
    private boolean isRecipeType(Object type) {
        if (type instanceof String) {
            return type.equals("Recipe");
        }
        if (type instanceof JSONArray) {
            JSONArray typeArray = (JSONArray) type;
            for (int i = 0; i < typeArray.length(); i++) {
                if (typeArray.optString(i).equals("Recipe")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Extracts the title, ingredients, instructions, and image from a Recipe JSON object.
     */
    private void extractRecipeData(JSONObject recipeJson) throws JSONException {
        // Extract title
        this.title = recipeJson.optString("name");

        // Extract ingredients
        if (recipeJson.has("recipeIngredient")) {
            JSONArray ingredientsArray = recipeJson.getJSONArray("recipeIngredient");
            for (int i = 0; i < ingredientsArray.length(); i++) {
                this.ingredientsList.add(ingredientsArray.getString(i));
            }
        }

        // Extract instructions
        if (recipeJson.has("recipeInstructions")) {
            Object instructionsData = recipeJson.get("recipeInstructions");
            extractInstructionSteps(instructionsData);
        }

        // Extract image (if available as an array)
        if (recipeJson.has("image")) {
            Object imageData = recipeJson.get("image");
            if (imageData instanceof JSONArray) {
                this.imageUrl = ((JSONArray) imageData).getString(0);
            }
        }
    }

    /**
     * Recursively extracts instruction step text from JSON-LD instruction data.
     * Handles HowToStep, HowToSection, and plain string formats.
     */
    private void extractInstructionSteps(Object data) throws JSONException {
        if (data instanceof JSONArray) {
            JSONArray array = (JSONArray) data;
            for (int i = 0; i < array.length(); i++) {
                extractInstructionSteps(array.get(i));
            }
        } else if (data instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) data;
            String type = jsonObject.optString("@type");

            if ("HowToStep".equals(type)) {
                String stepText = jsonObject.optString("text");
                instructionsList.add(stepText);
            } else if ("HowToSection".equals(type) || jsonObject.has("itemListElement")) {
                extractInstructionSteps(jsonObject.get("itemListElement"));
            } else if (jsonObject.has("text")) {
                instructionsList.add(jsonObject.getString("text"));
            }
        } else if (data instanceof String) {
            instructionsList.add((String) data);
        }
    }
}
