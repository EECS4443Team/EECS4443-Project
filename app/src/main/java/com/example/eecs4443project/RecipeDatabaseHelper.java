package com.example.eecs4443project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipeDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "RecipeDatabase.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "saved_recipes";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_INGREDIENTS = "ingredients";
    public static final String COLUMN_INSTRUCTIONS = "instructions";
    private static RecipeDatabaseHelper instance;

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_INGREDIENTS + " TEXT, " +
                    COLUMN_INSTRUCTIONS + " TEXT);";

    public RecipeDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // This is required to make "ON DELETE CASCADE" work in SQLite
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    /**
     * Retrieves a recipe from the database by its ID.
     * This method fetches the recipe details along with its associated ingredients and instructions.
     *
     * @param recipeId The ID of the recipe to retrieve.
     * @return A {@link Recipe} object containing the recipe details, ingredients, and instructions,
     * or {@code null} if no recipe was found with the given ID.
     */
    public Recipe getRecipe(long recipeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Recipe recipe = null;
        Cursor cursor = null;

        try {

            cursor = db.query(
                    TABLE_NAME,
                    null,
                    COLUMN_ID + "=?",
                    new String[]{String.valueOf(recipeId)},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {

                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String ingredientsRaw = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENTS));
                String instructionsRaw = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INSTRUCTIONS));

                List<String> ingredients = new ArrayList<>();
                if (!TextUtils.isEmpty(ingredientsRaw)) {
                    ingredients.addAll(Arrays.asList(ingredientsRaw.split("\n")));
                }

                List<String> instructions = new ArrayList<>();
                if (!TextUtils.isEmpty(instructionsRaw)) {
                    instructions.addAll(Arrays.asList(instructionsRaw.split("\n")));
                }

                recipe = new Recipe(id, title, ingredients, instructions);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return recipe;
    }
    public static synchronized RecipeDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new RecipeDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Extracts data from a {@link Recipe} object and saves it to the database.
     * Lists of ingredients and instructions are converted to newline-separated strings.
     *
     * @param recipe The Recipe object to save.
     * @return The row ID of the newly inserted row, or -1 if an error occurred.
     */
    public long saveRecipe(Recipe recipe) {
        String ingredientsStr = TextUtils.join("\n", recipe.getIngredients());
        String instructionsStr = TextUtils.join("\n", recipe.getInstructions());
        return saveRecipe(recipe.getTitle(), ingredientsStr, instructionsStr);
    }

    /**
     * Saves a recipe to the database using raw strings.
     *
     * @param title        The recipe title.
     * @param ingredients  The ingredients as a newline-separated string.
     * @param instructions The instructions as a newline-separated string.
     * @return The row ID of the newly inserted row, or -1 if an error occurred.
     */
    public long saveRecipe(String title, String ingredients, String instructions) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_INGREDIENTS, ingredients);
        values.put(COLUMN_INSTRUCTIONS, instructions);

        long id = db.insert(TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public Cursor getAllSavedRecipes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, null, null, null, null, COLUMN_ID + " DESC");
    }

    public boolean isRecipeSaved(String title) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_ID},
                COLUMN_TITLE + "=?", new String[]{title},
                null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return exists;
    }

    public void deleteRecipeByTitle(String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_TITLE + "=?", new String[]{title});
        db.close();
    }
}
