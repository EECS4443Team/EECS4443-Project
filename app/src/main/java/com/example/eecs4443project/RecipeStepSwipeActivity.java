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
import androidx.lifecycle.ViewModelProvider;
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

    private LinearLayoutManager linearLayoutManager;
    private GridLayoutManager gridLayoutManager;
    private PagerSnapHelper snapHelper;
    private int handMode = 1;
    private ScaleGestureDetector scaleGestureDetector;
    private RecipeViewModel viewModel;

    @SuppressLint({"ClickableViewAccessibility", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_step_swipe);

        viewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

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
        viewModel.setRawRecipeText(recipeText);

        // Corrected Lambda Syntax
        adapter = new RecipeAdapter(new ArrayList<>(), position -> {
            if (Boolean.TRUE.equals(viewModel.getIsGridView().getValue())) {
                Log.d("RecipeInteraction", "Grid item clicked: " + position);
                switchToSwipeAtPosition(position);
            }
        });
        recyclerView.setAdapter(adapter);

        // Observe ViewModel Data
        viewModel.getSteps().observe(this, steps -> {
            if (steps != null) {
                adapter.setSteps(steps);
                seekBar.setMax(!steps.isEmpty() ? steps.size() - 1 : 0);
                updateUI(viewModel.getCurrentStepPosition().getValue(), steps);
            }
        });

        viewModel.getIsGridView().observe(this, isGrid -> {
            if (Boolean.TRUE.equals(isGrid)) {
                applyGridLayout();
            } else {
                applySwipeLayout();
            }
        });

        viewModel.getCurrentStepPosition().observe(this, pos -> {
            updateUI(pos, viewModel.getSteps().getValue());
            if (!Boolean.TRUE.equals(viewModel.getIsGridView().getValue()) && pos != null) {
                recyclerView.smoothScrollToPosition(pos);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (!Boolean.TRUE.equals(viewModel.getIsGridView().getValue())) {
                    View centerView = snapHelper.findSnapView(linearLayoutManager);
                    if (centerView != null) {
                        int pos = linearLayoutManager.getPosition(centerView);
                        viewModel.setCurrentStepPosition(pos);
                    }
                }
            }
        });

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                if (handMode == 2 && !Boolean.TRUE.equals(viewModel.getIsGridView().getValue()) && detector.getScaleFactor() < 0.8f) {
                    viewModel.setGridView(true);
                    return true;
                }
                return false;
            }
        });

        recyclerView.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            return false;
        });

        prevButton.setOnClickListener(v -> viewModel.previousStep());

        nextButton.setOnClickListener(v -> {
            List<String> steps = viewModel.getSteps().getValue();
            Integer pos = viewModel.getCurrentStepPosition().getValue();
            if (steps != null && pos != null && pos == steps.size() - 1) {
                finish();
            } else {
                viewModel.nextStep();
            }
        });
    }

    private int calculateSpanCount() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int count = (int) (dpWidth / 150); 
        return Math.max(3, count);
    }

    private void switchToSwipeAtPosition(int position) {
        viewModel.setGridView(false);
        viewModel.setCurrentStepPosition(position);
    }

    private void applySwipeLayout() {
        Log.d("RecipeInteraction", "Applying Swipe Layout");
        recyclerView.setLayoutManager(linearLayoutManager);
        try {
            snapHelper.attachToRecyclerView(null);
            snapHelper.attachToRecyclerView(recyclerView);
        } catch (Exception e) {
            Log.e("RecipeInteraction", "Error attaching SnapHelper", e);
        }
        adapter.setGridMode(false);
        adapter.notifyDataSetChanged();
    }

    private void applyGridLayout() {
        Log.d("RecipeInteraction", "Applying Grid Layout");
        try {
            snapHelper.attachToRecyclerView(null);
        } catch (Exception e) {
            Log.e("RecipeInteraction", "Error detaching SnapHelper", e);
        }
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter.setGridMode(true);
        adapter.notifyDataSetChanged();
    }

    private void updateUI(Integer position, List<String> steps) {
        if (position == null || steps == null || position < 0 || position >= steps.size()) return;
        stepIndicator.setText("Step " + (position + 1));
        seekBar.setProgress(position);
        prevButton.setEnabled(position > 0);
        
        if (position == steps.size() - 1) {
            nextButton.setText("Finish");
        } else {
            nextButton.setText("Next");
        }
    }
}
