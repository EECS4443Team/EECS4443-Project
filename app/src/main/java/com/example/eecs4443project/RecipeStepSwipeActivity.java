package com.example.eecs4443project;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RecipeStepSwipeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private SeekBar seekBar;
    private TextView stepIndicator;
    private Button prevButton, nextButton;
    private List<String> steps = new ArrayList<>();

    private LinearLayoutManager linearLayoutManager;
    private GridLayoutManager gridLayoutManager;
    private PagerSnapHelper snapHelper;
    private boolean isGridView = false;
    private int handMode = 1;
    private ScaleGestureDetector scaleGestureDetector;

    @SuppressLint({"ClickableViewAccessibility", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_step_swipe);

        handMode = getIntent().getIntExtra("handMode", 1);

        recyclerView = findViewById(R.id.recipeRecyclerView);
        stepIndicator = findViewById(R.id.stepIndicator);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        seekBar = findViewById(R.id.seekBar);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        
        int spanCount = calculateSpanCount();
        gridLayoutManager = new GridLayoutManager(this, spanCount);
        
        snapHelper = new PagerSnapHelper();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String recipeText = getIntent().getStringExtra("recipe_text");
        processRecipeData(recipeText);

        recyclerView.setLayoutManager(linearLayoutManager);
        snapHelper.attachToRecyclerView(recyclerView);

        adapter = new RecipeAdapter(steps, position -> {
            if (isGridView) {
                Log.d("RecipeInteraction", "Grid item clicked: " + position);
                switchToSwipeAtPosition(position);
            }
        });
        recyclerView.setAdapter(adapter);

        seekBar.setMax(steps.size() > 0 ? steps.size() - 1 : 0);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (!isGridView) {
                    View centerView = snapHelper.findSnapView(linearLayoutManager);
                    if (centerView != null) {
                        int pos = linearLayoutManager.getPosition(centerView);
                        updateUI(pos);
                    }
                }
            }
        });

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                if (handMode == 2 && !isGridView && detector.getScaleFactor() < 0.8f) {
                    switchToGrid();
                    return true;
                }
                return false;
            }
        });

        recyclerView.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            return false;
        });

        prevButton.setOnClickListener(v -> {
            int current = linearLayoutManager.findFirstVisibleItemPosition();
            if (current > 0) recyclerView.smoothScrollToPosition(current - 1);
        });

        nextButton.setOnClickListener(v -> {
            int current = linearLayoutManager.findFirstVisibleItemPosition();
            if (current < steps.size() - 1) {
                recyclerView.smoothScrollToPosition(current + 1);
            } else {
                // Return to previous activity when Finish is clicked
                finish();
            }
        });

        updateUI(0);
    }

    /**
     * Calculates the number of columns for the grid view based on screen width.
     */
    private int calculateSpanCount() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int count = (int) (dpWidth / 150); 
        return Math.max(3, count);
    }

    /**
     * Switches the view from grid mode back to horizontal swipe mode at a specific step.
     */
    private void switchToSwipeAtPosition(int position) {
        if (!isGridView) return;
        isGridView = false;
        Log.d("RecipeInteraction", "Switching to Swipe at " + position);

        recyclerView.setLayoutManager(linearLayoutManager);
        try {
            snapHelper.attachToRecyclerView(null);
            snapHelper.attachToRecyclerView(recyclerView);
        } catch (Exception e) {
            Log.e("RecipeInteraction", "Error attaching SnapHelper", e);
        }

        adapter.setGridMode(false);
        adapter.notifyDataSetChanged();
        
        recyclerView.post(() -> {
            recyclerView.scrollToPosition(position);
            updateUI(position);
        });
    }

    /**
     * Switches the view from swipe mode to a grid overview of all steps.
     */
    private void switchToGrid() {
        if (isGridView) return;
        isGridView = true;
        Log.d("RecipeInteraction", "Switching to Grid");
        
        try {
            snapHelper.attachToRecyclerView(null);
        } catch (Exception e) {
            Log.e("RecipeInteraction", "Error detaching SnapHelper", e);
        }
        
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter.setGridMode(true);
        adapter.notifyDataSetChanged();
    }

    /**
     * Parses the recipe text and prepares the list of steps for the adapter.
     */
    private void processRecipeData(String rawText) {
        if (rawText == null) return;
        List<String> instructionSteps = parseSteps(rawText);
        steps.clear();
        steps.addAll(instructionSteps);
        if (steps.isEmpty()) {
            steps.add(rawText);
        }
    }

    /**
     * Updates the step indicator, progress bar, and navigation buttons.
     */
    private void updateUI(int position) {
        if (position < 0 || position >= steps.size()) return;
        stepIndicator.setText("Step " + (position + 1));
        seekBar.setProgress(position);
        prevButton.setEnabled(position > 0);
        
        // Always enabled to allow "Finish" click on the last step
        nextButton.setEnabled(true);

        if (position == steps.size() - 1) {
            nextButton.setText("Finish");
        } else {
            nextButton.setText("Next");
        }
    }

    /**
     * Parses raw text into a list of individual cooking instructions.
     */
    private List<String> parseSteps(String text) {
        List<String> list = new ArrayList<>();
        String[] lines = text.split("\n");
        boolean inInstructions = false;
        StringBuilder currentStep = new StringBuilder();
        for (String line : lines) {
            String cleanLine = line.trim().replaceAll("[\\*#]", "");
            String lower = cleanLine.toLowerCase();
            if (lower.contains("instructions:") || lower.contains("steps:") || lower.contains("directions:")) {
                inInstructions = true;
                continue;
            }
            if (inInstructions && !cleanLine.isEmpty()) {
                if (cleanLine.matches("^\\d+[\\.\\)].*")) {
                    if (currentStep.length() > 0) list.add(currentStep.toString().trim());
                    currentStep = new StringBuilder(cleanLine.replaceAll("^\\d+[\\.\\)]\\s*", ""));
                } else {
                    if (currentStep.length() > 0) currentStep.append(" ");
                    currentStep.append(cleanLine);
                }
            }
        }
        if (currentStep.length() > 0) list.add(currentStep.toString().trim());
        return list;
    }
}
