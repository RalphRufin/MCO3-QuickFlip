package com.mobdeve.s20.group7.mco2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s20.group7.mco2.models.CardItem

class CardAdapter(private val cardItems: List<CardItem>) :
    RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    inner class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val questionTextView: TextView = view.findViewById(R.id.tvQuestion)
        val answerTextView: TextView = view.findViewById(R.id.tvAnswer)

        fun bind(cardItem: CardItem) {
            questionTextView.text = cardItem.question
            answerTextView.text = cardItem.answer

            // Start with answer hidden
            answerTextView.visibility = View.GONE

            // Toggle visibility of answer on question click
            questionTextView.setOnClickListener {
                if (answerTextView.visibility == View.GONE) {
                    answerTextView.visibility = View.VISIBLE
                } else {
                    answerTextView.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(cardItems[position])
    }

    override fun getItemCount(): Int = cardItems.size
}
