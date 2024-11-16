package com.mobdeve.s20.group7.mco2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobdeve.s20.group7.mco2.models.DeckItem

class DeckAdapter(
    private val deckItems: List<DeckItem>,
    private val clickListener: (DeckItem) -> Unit // Normal click listener
) : RecyclerView.Adapter<DeckAdapter.DeckViewHolder>() {

    inner class DeckViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivDeckImage: ImageButton = view.findViewById(R.id.ivDeckImage)
        private val tvDeckTitle: TextView = view.findViewById(R.id.tvDeckTitle)

        init {
            // Normal click behavior
            view.setOnClickListener {
                clickListener(deckItems[adapterPosition])
            }
        }

        fun bind(deckItem: DeckItem, context: Context) {
            tvDeckTitle.text = deckItem.deckTitle
            Glide.with(context)
                .load(deckItem.deckImage)
                .placeholder(R.drawable.quickflipcutedeck)
                .into(ivDeckImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.deck_item, parent, false)
        return DeckViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeckViewHolder, position: Int) {
        holder.bind(deckItems[position], holder.itemView.context)
    }

    override fun getItemCount(): Int = deckItems.size
}
