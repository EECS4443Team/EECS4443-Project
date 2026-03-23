package com.example.eecs4443project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class RecipeDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "recipe_app.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_INGREDIENTS = "ingredients";
    public static final String TABLE_INSTRUCTIONS = "instructions";

    // Common Column Names
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_RECIPE_ID_FK = "recipe_id";

    // Recipes Columns
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_PREP_TIME = "prep_time";
    public static final String COL_COOK_TIME = "cook_time";
    public static final String COL_YIELD = "recipe_yield";

    // Ingredients Columns
    public static final String COL_INGREDIENT_NAME = "ingredient_name";

    // Instructions Columns
    public static final String COL_STEP_NUMBER = "step_number";
    public static final String COL_STEP_TEXT = "instruction_text";

    private static RecipeDatabaseHelper instance;

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
        // Create Recipe Table
        String CREATE_RECIPE_TABLE = "CREATE TABLE " + TABLE_RECIPES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECIPE_NAME + " TEXT NOT NULL, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_PREP_TIME + " TEXT, " +
                COL_COOK_TIME + " TEXT, " +
                COL_YIELD + " TEXT" + ")";

        // Create Ingredients Table
        String CREATE_INGREDIENTS_TABLE = "CREATE TABLE " + TABLE_INGREDIENTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_RECIPE_ID_FK + " INTEGER, " +
                COL_INGREDIENT_NAME + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + COLUMN_RECIPE_ID_FK + ") REFERENCES " + TABLE_RECIPES + "(" + COLUMN_ID + ") ON DELETE CASCADE" + ")";

        // Create Instructions Table
        String CREATE_INSTRUCTIONS_TABLE = "CREATE TABLE " + TABLE_INSTRUCTIONS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_RECIPE_ID_FK + " INTEGER, " +
                COL_STEP_NUMBER + " INTEGER, " +
                COL_STEP_TEXT + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + COLUMN_RECIPE_ID_FK + ") REFERENCES " + TABLE_RECIPES + "(" + COLUMN_ID + ") ON DELETE CASCADE" + ")";

        db.execSQL(CREATE_RECIPE_TABLE);
        db.execSQL(CREATE_INGREDIENTS_TABLE);
        db.execSQL(CREATE_INSTRUCTIONS_TABLE);
    }

    /**
     * Inserts a new recipe into the database.
     * This method saves the core recipe details into the recipes table, then inserts
     * all associated ingredients and instructions into their respective tables.
     * After a successful insertion, the provided {@link Recipe} object's ID is updated
     * with the newly generated database ID.
     *
     * @param recipe The {@link Recipe} object containing the data to be stored.
     */
    public void addRecipe(Recipe recipe) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues recipeValues = new ContentValues();
        recipeValues.put(COL_RECIPE_NAME, recipe.getTitle());


        long recipeId = db.insert(TABLE_RECIPES, null, recipeValues);
        recipe.id = recipeId;

        for (String ingredient : recipe.getIngredients()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_RECIPE_ID_FK, recipeId);
            values.put(COL_INGREDIENT_NAME, ingredient);
            db.insert(TABLE_INGREDIENTS, null, values);
        }


        int stepNum = 1;
        for (String step : recipe.getInstructions()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_RECIPE_ID_FK, recipeId);
            values.put(COL_STEP_NUMBER, stepNum++);
            values.put(COL_STEP_TEXT, step);
            db.insert(TABLE_INSTRUCTIONS, null, values);
        }

        db.close();
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


        Cursor cursor = db.query(TABLE_RECIPES, null, COLUMN_ID + "=?",
                new String[]{String.valueOf(recipeId)}, null, null, null);

        if (cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_RECIPE_NAME));
            cursor.close();


            List<String> ingredients = new ArrayList<>();
            Cursor ingCursor = db.query(TABLE_INGREDIENTS, new String[]{COL_INGREDIENT_NAME},
                    COLUMN_RECIPE_ID_FK + "=?", new String[]{String.valueOf(recipeId)}, null, null, null);
            while (ingCursor.moveToNext()) {
                ingredients.add(ingCursor.getString(0));
            }
            ingCursor.close();


            List<String> instructions = new ArrayList<>();
            Cursor insCursor = db.query(TABLE_INSTRUCTIONS, new String[]{COL_STEP_TEXT},
                    COLUMN_RECIPE_ID_FK + "=?", new String[]{String.valueOf(recipeId)}, null, null, COL_STEP_NUMBER + " ASC");
            while (insCursor.moveToNext()) {
                instructions.add(insCursor.getString(0));
            }
            insCursor.close();

            return new Recipe(id, title, ingredients, instructions);
        }
        return null;
    }
    //RecipeDatabaseHelper.getInstance(this).getRecipe(recipeId);
    //RecipeDatabaseHelper.getInstance(this).addRecipe(recipeId);
    public static synchronized RecipeDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new RecipeDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple strategy: drop tables and recreate them
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INSTRUCTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        onCreate(db);
    }
}
