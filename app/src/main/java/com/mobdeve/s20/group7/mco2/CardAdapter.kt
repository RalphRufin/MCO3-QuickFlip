package com.mobdeve.s20.group7.mco2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s20.group7.mco2.models.CardItem

class CardAdapter(
    private val cardItems: List<CardItem>, // List of cards
    private val onEditClick: (Int) -> Unit // Callback for the edit button click
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    inner class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val questionTextView: TextView = view.findViewById(R.id.tvQuestion)
        val answerTextView: TextView = view.findViewById(R.id.tvAnswer)
        val editButton: ImageButton = view.findViewById(R.id.btnQuestion)

        fun bind(cardItem: CardItem, position: Int) {
            questionTextView.text = cardItem.question
            answerTextView.text = cardItem.answer

            // Start with the answer hidden
            answerTextView.visibility = View.GONE

            // Toggle visibility of the answer on question click
            questionTextView.setOnClickListener {
                answerTextView.visibility =
                    if (answerTextView.visibility == View.GONE) View.VISIBLE else View.GONE
            }

            // Handle edit button click
            editButton.setOnClickListener {
                onEditClick(position) // Notify the activity about the click
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(cardItems[position], position)
    }

    override fun getItemCount(): Int = cardItems.size
}
