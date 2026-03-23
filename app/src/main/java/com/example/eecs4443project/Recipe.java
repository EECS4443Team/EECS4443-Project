package com.example.eecs4443project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Recipe implements Serializable {
    public long id ;
    public String title;
    public List<String> ingredients;
    public List<String> instructions;
    public String imageUrl;
    public String getTitle() {
        return title;
    }


    public List<String> getIngredients() {
        return ingredients;
    }



    public List<String> getInstructions() {
        return instructions;
    }



    public Recipe(long id, String title, List<String> ingredients, List<String> instructions) {
        this.id = id;
        this.title = title;
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
        this.instructions = instructions != null ? instructions : new ArrayList<>();
    }
    public Recipe(String title, String imageUrl, List<String> ingredients, List<String> instructions) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
        this.instructions = instructions != null ? instructions : new ArrayList<>();
    }
}
