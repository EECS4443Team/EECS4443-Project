package com.example.eecs4443project;

import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;

public class HtmlParserTest {

    @Test
    public void testDynamicParser() {
//        HtmlParser parser1 = new HtmlParser("https://www.allrecipes.com/recipe/23600/worlds-best-lasagna/");
//        parser1.parseDynamicRecipe();
        System.out.println("1st");
        HtmlParser parser1 = new HtmlParser("https://www.spendwithpennies.com/easy-homemade-lasagna/");
        HtmlParser parser2 = new HtmlParser("https://www.spendwithpennies.com/the-best-chili-recipe/");
        HtmlParser parser3 = new HtmlParser("https://www.simplyrecipes.com/recipes/lasagna/");
    }
    @Test
    public void testPrint(){
        HtmlParser parser1 = new HtmlParser("https://www.allrecipes.com/recipe/23600/worlds-best-lasagna/");
       System.out.println( parser1.toString());
    }
}
