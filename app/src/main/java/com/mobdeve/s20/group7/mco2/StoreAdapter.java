package com.mobdeve.s20.group7.mco2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

    private final List<StoreItem> storeItems;
    private final Context context;

    public StoreAdapter(Context context, List<StoreItem> storeItems) {
        this.context = context;
        this.storeItems = storeItems;
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.store_item, parent, false);
        return new StoreViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        StoreItem currentItem = storeItems.get(position);

        holder.tvItemCost.setText(currentItem.getPointsCost() + " points");
        holder.tvItemName.setText(currentItem.getName());
        holder.btnItemIcon.setImageResource(currentItem.getIconResource());

        // Set click listener for the item (optional)
        holder.btnItemIcon.setOnClickListener(v -> {
            // Handle item click
            Toast.makeText(context, "Clicked on " + currentItem.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return storeItems.size();
    }

    public static class StoreViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemCost, tvItemName;
        ImageButton btnItemIcon;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemCost = itemView.findViewById(R.id.tvItemCost);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            btnItemIcon = itemView.findViewById(R.id.btnItemIcon);
        }
    }
}

