package com.mobdeve.s20.group7.mco2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
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
            val drawableResource = getDrawableResource(deckItem.getDeckImage())
            deckImageView.setImageResource(
                if (drawableResource != 0) drawableResource else R.drawable.default_image
            )
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

        private fun getDrawableResource(imagePath: String): Int {
            return itemView.context.resources.getIdentifier(imagePath, "drawable", itemView.context.packageName)
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
