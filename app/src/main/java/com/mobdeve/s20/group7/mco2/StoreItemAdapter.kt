package com.mobdeve.s20.group7.mco2

import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s20.group7.mco2.models.StoreItem

class StoreItemAdapter(
    private var storeItems: List<StoreItem>,
    private val onItemClick: (StoreItem) -> Unit
) : RecyclerView.Adapter<StoreItemAdapter.StoreItemViewHolder>() {

    inner class StoreItemViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val tvItemName: TextView = itemView.findViewById(R.id.tvItemName)
        val tvItemDescription: TextView = itemView.findViewById(R.id.tvItemDescription)
        val tvItemPrice: TextView = itemView.findViewById(R.id.tvItemPrice)
        val itemContainer: android.view.View = itemView.findViewById(R.id.itemContainer)
        val ivItemIcon: ImageView = itemView.findViewById(R.id.ivItemIcon)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): StoreItemViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_store, parent, false)
        return StoreItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoreItemViewHolder, position: Int) {
        val item = storeItems[position]
        holder.tvItemName.text = item.name
        holder.tvItemDescription.text = item.description
        holder.tvItemPrice.text = "Price: ${item.price} points"

        // Set icon
        holder.ivItemIcon.setImageResource(item.iconResId)

        // Disable item if already purchased
        holder.itemContainer.isEnabled = !item.isPurchased
        holder.itemContainer.alpha = if (item.isPurchased) 0.5f else 1f

        holder.itemContainer.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = storeItems.size
}