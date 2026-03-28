package com.example.eecs4443project;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SavedRecipesActivity extends AppCompatActivity {

    private SavedRecipesViewModel viewModel;
    private final List<SavedRecipe> adapterList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_results); // Reusing search results layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView title = findViewById(R.id.resultsTitle);
        if (title != null) {
            title.setText("Saved Recipes");
            title.setGravity(Gravity.CENTER);
            title.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        }

        viewModel = new ViewModelProvider(this).get(SavedRecipesViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.recipeRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        SavedRecipesAdapter adapter = new SavedRecipesAdapter(adapterList);
        recyclerView.setAdapter(adapter);

        // Observe saved recipes from ViewModel
        viewModel.getSavedRecipesList().observe(this, recipes -> {
            adapterList.clear();
            adapterList.addAll(recipes);
            adapter.notifyDataSetChanged();
        });

        viewModel.loadSavedRecipes();

        // Swipe left to delete with red background, trash icon, and confirmation
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private final ColorDrawable background = new ColorDrawable(Color.RED);
            private final Drawable deleteIcon = ContextCompat.getDrawable(SavedRecipesActivity.this, android.R.drawable.ic_menu_delete);

            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                if (dX < 0) {
                    // Draw red background
                    background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(),
                            itemView.getRight(), itemView.getBottom());
                    background.draw(c);

                    // Draw trash icon centered vertically, with margin from the right edge
                    if (deleteIcon != null) {
                        int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + iconMargin;
                        int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
                        int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
                        int iconRight = itemView.getRight() - iconMargin;
                        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        deleteIcon.setTint(Color.WHITE);
                        deleteIcon.draw(c);
                    }
                }
                super.onChildDraw(c, rv, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                SavedRecipe recipe = adapterList.get(position);

                new AlertDialog.Builder(SavedRecipesActivity.this)
                        .setTitle("Delete Recipe")
                        .setMessage("Are you sure you want to delete \"" + recipe.getTitle() + "\"?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            viewModel.deleteRecipeFromDatabase(recipe.getTitle());
                            adapterList.remove(position);
                            adapter.notifyItemRemoved(position);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            adapter.notifyItemChanged(position);
                        })
                        .setOnCancelListener(dialog -> {
                            adapter.notifyItemChanged(position);
                        })
                        .show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    class SavedRecipesAdapter extends RecyclerView.Adapter<SavedRecipesAdapter.ViewHolder> {
        private final List<SavedRecipe> recipes;

        SavedRecipesAdapter(List<SavedRecipe> recipes) {
            this.recipes = recipes;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SavedRecipe recipe = recipes.get(position);
            holder.recipeName.setText(recipe.getTitle());
            holder.recipeImage.setImageResource(R.drawable.baseline_fastfood_24);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(SavedRecipesActivity.this, RecipeDetailActivity.class);
                intent.putExtra("acquisition_mode", "ai");
                intent.putExtra("recipe_text", recipe.toFullText());
                intent.putExtra("from_saved", true);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return recipes.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView recipeImage;
            TextView recipeName;

            ViewHolder(View itemView) {
                super(itemView);
                recipeImage = itemView.findViewById(R.id.recipeImage);
                recipeName = itemView.findViewById(R.id.recipeName);
            }
        }
    }
}
