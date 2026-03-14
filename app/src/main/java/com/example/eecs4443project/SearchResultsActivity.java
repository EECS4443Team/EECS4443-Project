package com.example.eecs4443project;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_results);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.recipeRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<String> recipes = new ArrayList<>();
        recipes.add("Sample Recipe 1");
        recipes.add("Sample Recipe 2");
        recipes.add("Sample Recipe 3");

        recyclerView.setAdapter(new RecyclerView.Adapter<RecipeViewHolder>() {
            @NonNull
            @Override
            public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_recipe, parent, false);
                return new RecipeViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
                holder.recipeName.setText(recipes.get(position));
                holder.recipeImage.setImageResource(R.mipmap.ic_launcher);
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(SearchResultsActivity.this, RecipeDetailActivity.class);
                    intent.putExtra("acquisition_mode", "local");
                    intent.putExtra("recipe_name", recipes.get(position));
                    startActivity(intent);
                });
            }

            @Override
            public int getItemCount() { return recipes.size(); }
        });
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView recipeImage;
        TextView recipeName;

        RecipeViewHolder(View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            recipeName = itemView.findViewById(R.id.recipeName);
        }
    }
}
