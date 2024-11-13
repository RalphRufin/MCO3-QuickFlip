package com.mobdeve.s20.group7.mco2

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s20.group7.mco2.models.DeckItem

class CardListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_list)

        val deckItem = intent.getSerializableExtra("deck_item") as? DeckItem
        if (deckItem != null) {
            Log.d("CardListActivity", "Received deck: ${deckItem.getDeckTitle()}")
            // Use the deckItem to set up RecyclerView or display data
        } else {
            Log.e("CardListActivity", "Failed to receive deck item from intent.")
        }
    }
}
