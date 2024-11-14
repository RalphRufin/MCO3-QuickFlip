package com.mobdeve.s20.group7.mco2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.mobdeve.s20.group7.mco2.models.CardItem
import com.mobdeve.s20.group7.mco2.models.DeckItem

class CardListActivity : AppCompatActivity() {

    private lateinit var rvCards: RecyclerView
    private lateinit var cardAdapter: CardAdapter
    private var cardItems: ArrayList<CardItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_list)

        // Get DeckItem from intent as Parcelable
        val deckItem: DeckItem? = intent.getParcelableExtra("deck_item")
        if (deckItem != null) {
            cardItems = deckItem.cardItems as ArrayList<CardItem>
        }

        rvCards = findViewById(R.id.rvCards)
        rvCards.layoutManager = LinearLayoutManager(this)

        // Set up adapter
        cardAdapter = CardAdapter(cardItems)
        rvCards.adapter = cardAdapter
    }
}
