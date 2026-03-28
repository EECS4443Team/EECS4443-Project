package com.example.eecs4443project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * RecyclerView adapter for displaying recipe cooking steps.
 * Supports two view modes:
 * - Card mode: shows one step at a time in a large swipeable card
 * - Grid mode: shows all steps in a compact grid overview
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.StepViewHolder> {

    private static final int VIEW_TYPE_CARD = 1;
    private static final int VIEW_TYPE_GRID = 2;

    private final List<String> stepsList;
    private final OnStepClickListener clickListener;
    private boolean isGridMode = false;

    /**
     * Listener interface for when a step is clicked (used in grid mode).
     */
    public interface OnStepClickListener {
        void onStepClicked(int position);
    }

    public RecipeAdapter(List<String> stepsList, OnStepClickListener clickListener) {
        this.stepsList = stepsList;
        this.clickListener = clickListener;
    }

    public void setGridMode(boolean gridMode) {
        this.isGridMode = gridMode;
    }

    @Override
    public int getItemViewType(int position) {
        return isGridMode ? VIEW_TYPE_GRID : VIEW_TYPE_CARD;
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId;
        if (viewType == VIEW_TYPE_GRID) {
            layoutId = R.layout.item_recipe_step_grid;
        } else {
            layoutId = R.layout.item_recipe_step_card;
        }

        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new StepViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        // Set the step content text
        holder.stepContentText.setText(stepsList.get(position));

        // Set the step number (only present in grid layout)
        if (holder.stepNumberText != null) {
            holder.stepNumberText.setText(String.valueOf(position + 1));
        }

        // Set the click listener for the overlay
        holder.clickOverlay.setOnClickListener(view -> {
            if (clickListener != null) {
                clickListener.onStepClicked(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stepsList.size();
    }

    /**
     * ViewHolder that holds references to the views in each step item.
     */
    static class StepViewHolder extends RecyclerView.ViewHolder {
        TextView stepContentText;
        TextView stepNumberText;  // Only exists in the grid layout
        View clickOverlay;

        StepViewHolder(View itemView, int viewType) {
            super(itemView);
            stepContentText = itemView.findViewById(R.id.tvStepContent);
            clickOverlay = itemView.findViewById(R.id.clickOverlay);

            if (viewType == VIEW_TYPE_GRID) {
                stepNumberText = itemView.findViewById(R.id.tvStepNumber);
            }
        }
    }
}
