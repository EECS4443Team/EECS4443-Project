package com.example.eecs4443project;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple strategy: drop tables and recreate them
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INSTRUCTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        onCreate(db);
    }
}
