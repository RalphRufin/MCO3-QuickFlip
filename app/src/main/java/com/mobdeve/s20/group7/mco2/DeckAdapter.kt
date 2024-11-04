package com.mobdeve.s20.group7.mco2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DeckAdapter(
    private val deckItems: List<DeckItem>,
    private val onDeckClick: ((DeckItem) -> Unit)? = null // Optional parameter with a default value
) : RecyclerView.Adapter<DeckAdapter.DeckViewHolder>() {

    inner class DeckViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deckImageView: ImageView = itemView.findViewById(R.id.ivDeckImage)
        private val deckTitleView: TextView = itemView.findViewById(R.id.tvDeckTitle)

        fun bind(deckItem: DeckItem) {
            val drawableResource = getDrawableResource(deckItem.getDeckImage())
            if (drawableResource != 0) {
                deckImageView.setImageResource(drawableResource)
            } else {
                deckImageView.setImageResource(R.drawable.default_image)
            }

            deckTitleView.text = deckItem.getDeckTitle()

            // Set up click listener only if onDeckClick is not null
            itemView.setOnClickListener {
                onDeckClick?.let { clickHandler -> clickHandler(deckItem) }
            }
        }

        private fun getDrawableResource(imagePath: String): Int {
            return itemView.context.resources.getIdentifier(imagePath, "drawable", itemView.context.packageName)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.deck_item, parent, false)
        return DeckViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DeckViewHolder, position: Int) {
        val currentItem = deckItems[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return deckItems.size
    }
}
