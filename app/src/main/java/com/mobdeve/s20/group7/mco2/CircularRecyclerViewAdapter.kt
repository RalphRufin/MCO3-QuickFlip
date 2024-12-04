package com.mobdeve.s20.group7.mco2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobdeve.s20.group7.mco2.models.DeckItem

class CircularRecyclerViewAdapter(
    private val decks: List<DeckItem>,
    private val onDeckClickListener: (DeckItem) -> Unit
) : RecyclerView.Adapter<CircularRecyclerViewAdapter.DeckViewHolder>() {

    companion object {
        private const val INFINITE_SCROLL_MULTIPLIER = 1000
    }

    inner class DeckViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deckImage: ImageView = itemView.findViewById(R.id.ivDeckImage)
        private val deckTitle: TextView = itemView.findViewById(R.id.tvDeckTitle)

        fun bind(deckItem: DeckItem) {
            // Set deck title
            deckTitle.text = deckItem.deckTitle

            // Load deck image using Glide with fade-in animation
            Glide.with(itemView.context)
                .load(deckItem.deckImage)
                .placeholder(R.drawable.quickflipcutedeck)
                .error(R.drawable.quickflipcutedeck)
                .into(deckImage)

            // Set click listener with slight scale animation
            itemView.setOnClickListener {
                it.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction {
                        it.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                        onDeckClickListener(deckItem)
                    }
                    .start()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.deck_item, parent, false)
        return DeckViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeckViewHolder, position: Int) {
        // Use modulo to create true circular scrolling
        val actualPosition = position % decks.size
        holder.bind(decks[actualPosition])
    }

    override fun getItemCount(): Int {
        return when {
            decks.isEmpty() -> 0
            decks.size == 1 -> 1  // No scrolling for single deck
            else -> decks.size * 2  // Alternate between decks for more than one deck
        }
    }
}