package com.mobdeve.s20.group7.mco2


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobdeve.s20.group7.mco2.models.DeckItem

class DeckAdapter(
    private val deckItems: List<DeckItem>,
    private val onDeckClick: ((DeckItem) -> Unit)? = null
) : RecyclerView.Adapter<DeckAdapter.DeckViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class DeckViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deckImageView: ImageView = itemView.findViewById(R.id.ivDeckImage)
        private val deckTitleView: TextView = itemView.findViewById(R.id.tvDeckTitle)

        fun bind(deckItem: DeckItem, isSelected: Boolean) {
            val imageUrl = deckItem.getDeckImage() // Assuming deckItem.getDeckImage() returns the image URL

            // Load image from URL using Glide
            Glide.with(itemView.context)
                .load(imageUrl)  // This can be a URL or a URI
                .placeholder(R.drawable.quickflipcutedeck) // Placeholder until the image loads
                .error(R.drawable.quickflipcutedeck) // Fallback image on error
                .into(deckImageView)

            deckTitleView.text = deckItem.getDeckTitle()

            // Change background to indicate selection
            itemView.setBackgroundColor(
                if (isSelected) ContextCompat.getColor(itemView.context, R.color.light_gray)
                else ContextCompat.getColor(itemView.context, android.R.color.transparent)
            )

            itemView.setOnClickListener {
                onDeckClick?.invoke(deckItem)
                notifyItemChanged(selectedPosition) // Deselect the old item
                selectedPosition = adapterPosition
                notifyItemChanged(selectedPosition) // Highlight the new item
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.deck_item, parent, false)
        return DeckViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DeckViewHolder, position: Int) {
        holder.bind(deckItems[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = deckItems.size
}
