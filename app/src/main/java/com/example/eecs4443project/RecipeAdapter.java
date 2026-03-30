package com.example.eecs4443project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {
    private List<String> steps;
    private OnItemClickListener listener;
    private boolean isGridMode = false;

    private static final int VIEW_TYPE_CARD = 1;
    private static final int VIEW_TYPE_GRID = 2;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setGridMode(boolean isGrid) {
        this.isGridMode = isGrid;
    }

    public RecipeAdapter(List<String> steps, OnItemClickListener listener) {
        this.steps = steps;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return isGridMode ? VIEW_TYPE_GRID : VIEW_TYPE_CARD;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = (viewType == VIEW_TYPE_GRID) 
                ? R.layout.item_recipe_step_grid 
                : R.layout.item_recipe_step_card;
        
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvStepContent.setText(steps.get(position));
        
        if (holder.tvStepNumber != null) {
            holder.tvStepNumber.setText(String.valueOf(position + 1));
        }

        holder.clickOverlay.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() { return steps.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStepContent;
        TextView tvStepNumber;
        View clickOverlay;

        ViewHolder(View itemView, int viewType) {
            super(itemView);
            tvStepContent = itemView.findViewById(R.id.tvStepContent);
            clickOverlay = itemView.findViewById(R.id.clickOverlay);
            if (viewType == VIEW_TYPE_GRID) {
                tvStepNumber = itemView.findViewById(R.id.tvStepNumber);
            }
        }
    }
}
