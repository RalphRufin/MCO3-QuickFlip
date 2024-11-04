package com.mobdeve.s20.group7.mco2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BrowseActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_browse)

        val recyclerViewDecks = findViewById<RecyclerView>(R.id.deck_recycler_view)

        // Creating dummy data (Simplified for the static deck)
        val dummyDecks = listOf(
            DeckItem("quickflipdeckicon", "Sample Deck").apply {
                addCardItem(CardItem("What is the capital of France?", "Paris"))
                addCardItem(CardItem("What is 2 + 2?", "4"))
            }
        )

        // Setting up the RecyclerView (Simplified with a single static deck)
        recyclerViewDecks.layoutManager = GridLayoutManager(this, 2)
        recyclerViewDecks.adapter = DeckAdapter(dummyDecks) { deck ->

            val intent = Intent(this, SpeedTActivity::class.java)
            startActivity(intent)

        }
        setupBaseComponents()
    }
}

// intent.putExtra("deckTitle", deck.getDeckTitle()) Passing cards to the next activity (dummy data)
// intent.putExtra("deckCards", ArrayList(deck.cardItems))
// startActivity(intent)
// }
//
// /*
// // Original Dummy Data (for multiple decks)
// val dummyDecks = listOf(
// DeckItem("history_deck_image", "History Deck").apply {
// addCardItem(CardItem("Who discovered America?", "Christopher Columbus"))
// addCardItem(CardItem("What was the Treaty of Versailles?", "The Treaty of Versailles ended World War I."))
// },
// DeckItem("math_deck_image", "Math Deck").apply {
// addCardItem(CardItem("What is 2 + 2?", "4"))
// addCardItem(CardItem("What is the value of pi?", "3.14159..."))
// },
// DeckItem("science_deck_image", "Science Deck").apply {
// addCardItem(CardItem("What is the chemical symbol for water?", "H2O"))
// addCardItem(CardItem("What is the largest planet in our solar system?", "Jupiter"))
// },
// DeckItem("geography_deck_image", "Geography Deck").apply {
// addCardItem(CardItem("What is the capital of France?", "Paris"))
// addCardItem(CardItem("Which river runs through Paris?", "Seine River"))
// },
// DeckItem("literature_deck_image", "Literature Deck").apply {
// addCardItem(CardItem("Who wrote 'Romeo and Juliet'?", "William Shakespeare"))
// addCardItem(CardItem("In what century did Shakespeare live?", "16th-17th century"))
// }
// )
//
// // Setting up the RecyclerView (Multiple decks)
// recyclerViewDecks.layoutManager = GridLayoutManager(this, 2)
// recyclerViewDecks.adapter = DeckAdapter(dummyDecks) { deck ->
// // On deck click, open com.mobdeve.s20.group7.mco2.SpeedTActivity with the selected deck
// val intent = Intent(this, com.mobdeve.s20.group7.mco2.SpeedTActivity::class.java)
// intent.putExtra("deckTitle", deck.getDeckTitle())
// intent.putExtra("deckCards", ArrayList(deck.cardItems))  // Passing cards to the next activity
// startActivity(intent)
// }
// */
// }