package com.example.eecs4443project;

import android.nfc.Tag;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlParser {
    //TODO : need to search url to search
    String url;
    String title;
    JSONArray ingredients = new JSONArray();
    private static final String TAG = "HtmlParser";
    //TODO : need to limit ingredients
    @Override
    public String toString() {
        return "HtmlParser{" +
                "recipeIngredients=" + ingredients +
                "recipeInstructions=" + recipeInstructions +
                '}';
    }

    JSONArray recipeInstructions = new JSONArray();

    public HtmlParser(String url) {
        this.url = url;
        parseDynamicRecipe();
    }
    /**
     * TODO search recipe based on ingredients
     * @param ingredients ; ingredients to search
     */
    public void searchRecipe (JSONArray ingredients) {

    }
    public void parseDynamicRecipe() {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36")
                    .get();

            // Search for <script type="application/ld+json"> tags
            Elements scripts = doc.select("script[type=application/ld+json]");

            for (Element script : scripts) {
                String content = script.data().trim();
                if (content.isEmpty()) continue;

                // Treat the root as JSONArray
                JSONArray jsonArray;
                if (content.startsWith("{")) {
                    JSONObject obj = new JSONObject(content);
                    // For @graph structure like Yoast SEO
                    if (obj.has("@graph")) {
                        jsonArray = obj.getJSONArray("@graph");
                    } else {
                        jsonArray = new JSONArray().put(obj);
                    }
                } else {
                    jsonArray = new JSONArray(content);
                }

                // Find type : @Recipe
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject item = jsonArray.getJSONObject(i);
                    Object type = item.opt("@type");

                    // Since type object can be an array, check whether it contains @Recipe or @Recipe itself
                    if (isRecipeType(type)) {
                        processRecipe(item);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper to check @type is Recipe
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

    private void processRecipe(JSONObject recipe) throws JSONException {

        this.title = recipe.optString("name");
//        Log.d(TAG, "Recipe Found : " + recipe.optString("name"));
        //  Ingredients
        if (recipe.has("recipeIngredient")) {
            this.ingredients = recipe.getJSONArray("recipeIngredient");
//            Log.d(TAG, "Ingredient : " + this.ingredients);
            System.out.println(this.ingredients);
            for (int i = 0; i < this.ingredients.length(); i++) {
                System.out.println("-" + this.ingredients.getString(i));
            }
        }

        // Instructions
        if (recipe.has("recipeInstructions")) {
//            System.out.println("\n[Instructions]");
            Object instructions = recipe.get("recipeInstructions");
            extractSteps(instructions);
        }
    }

    //Extract text from HowToSection or HowToStep recursively
    private void extractSteps(Object obj) throws JSONException {
        if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray) obj;
            for (int i = 0; i < array.length(); i++) {
                extractSteps(array.get(i));
            }
        } else if (obj instanceof JSONObject) {
            JSONObject node = (JSONObject) obj;
            String type = node.optString("@type");

            if (type.equals("HowToStep")) {
                String stepText = node.optString("text");
                recipeInstructions.put(stepText);
            } else if (type.equals("HowToSection") || node.has("itemListElement")) {
                // traverse list items of HowToSection object
                if (node.has("name")) System.out.println("<< Section: " + node.getString("name") + " >>");
                extractSteps(node.get("itemListElement"));
            } else if (node.has("text")) {
                String stepText = node.getString("text");
                recipeInstructions.put(stepText);
            }
        } else if (obj instanceof String) {
            // If it is just a String, print it
            recipeInstructions.put(obj);
        }
    }
}
