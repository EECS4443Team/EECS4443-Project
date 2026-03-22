package com.example.eecs4443project;

import org.json.JSONArray;

import java.io.Serializable;

public class Recipe implements Serializable {
    public String title;
    public JSONArray ingredients;
    public JSONArray instructions;

    public Recipe(String title, JSONArray ingredients, JSONArray instructions) {
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }

}
