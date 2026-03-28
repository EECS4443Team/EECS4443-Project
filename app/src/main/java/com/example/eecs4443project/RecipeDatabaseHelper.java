package com.example.eecs4443project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Manages the SQLite database for storing saved recipes.
 * Provides methods to save, retrieve, check, and delete recipes.
 */
public class RecipeDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "RecipeDatabase.db";
    private static final int DATABASE_VERSION = 1;

    // Table and column names
    public static final String TABLE_NAME = "saved_recipes";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_INGREDIENTS = "ingredients";
    public static final String COLUMN_INSTRUCTIONS = "instructions";

    // SQL statement to create the saved_recipes table
    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_INGREDIENTS + " TEXT, " +
                    COLUMN_INSTRUCTIONS + " TEXT);";

    public RecipeDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }

    /**
     * Saves a recipe to the database.
     *
     * @param title        The recipe title.
     * @param ingredients  The ingredients as a newline-separated string.
     * @param instructions The instructions as a newline-separated string.
     * @return The row ID of the new row, or -1 if saving failed.
     */
    public long saveRecipe(String title, String ingredients, String instructions) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_INGREDIENTS, ingredients);
        values.put(COLUMN_INSTRUCTIONS, instructions);

        long newRowId = database.insert(TABLE_NAME, null, values);
        database.close();
        return newRowId;
    }

    /**
     * Gets all saved recipes, ordered by most recently saved first.
     *
     * @return A Cursor pointing to the results. Caller must close it when done.
     */
    public Cursor getAllSavedRecipes() {
        SQLiteDatabase database = this.getReadableDatabase();
        return database.query(
                TABLE_NAME, null, null, null, null, null,
                COLUMN_ID + " DESC"
        );
    }

    /**
     * Checks if a recipe with the given title already exists in the database.
     *
     * @param title The recipe title to search for.
     * @return true if a recipe with this title is saved, false otherwise.
     */
    public boolean isRecipeSaved(String title) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.query(
                TABLE_NAME,
                new String[]{COLUMN_ID},
                COLUMN_TITLE + "=?",
                new String[]{title},
                null, null, null
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /**
     * Deletes all recipes with the given title from the database.
     *
     * @param title The title of the recipe to delete.
     */
    public void deleteRecipeByTitle(String title) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_NAME, COLUMN_TITLE + "=?", new String[]{title});
        database.close();
    }
}
