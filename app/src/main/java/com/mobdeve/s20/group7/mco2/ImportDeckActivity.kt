package com.mobdeve.s20.group7.mco2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s20.group7.mco2.models.CardItem
import com.mobdeve.s20.group7.mco2.models.DeckItem

class ImportDeckActivity : AppCompatActivity() {

    private lateinit var deckTitleText: TextView
    private lateinit var rvCards: RecyclerView
    private lateinit var importDeckButton: Button
    private lateinit var viewCardsButton: Button

    private var cardList: ArrayList<CardItem> = ArrayList()
    private lateinit var deckItem: DeckItem // Deck information

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_import)

        // Initialize views
        deckTitleText = findViewById(R.id.deckTitleText)
        rvCards = findViewById(R.id.rvCards)
        importDeckButton = findViewById(R.id.importDeck)
        viewCardsButton = findViewById(R.id.btnViewCards)

        // Retrieve the passed deckItem from the intent
        deckItem = intent.getParcelableExtra("deck_item")!!

        // Set up RecyclerView for displaying cards
        rvCards.layoutManager = LinearLayoutManager(this)
        val cardAdapter = CardAdapter(deckItem.cardItems)
        rvCards.adapter = cardAdapter

        // Set the deck title
        deckTitleText.text = deckItem.deckTitle

        // Import deck button functionality
        importDeckButton.setOnClickListener {
            importDeckToFirestore()
        }

        viewCardsButton.setOnClickListener {
            openCardViewerActivity()
        }
    }

    private fun importDeckToFirestore() {
        val firestore = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "User is not authenticated!", Toast.LENGTH_LONG).show()
            return
        }

        // Generate a new deck ID
        val newDeckId = firestore.collection("deck_items").document().id

        // Prepare the new deck data, including the cards
        val deckData = hashMapOf(
            "id" to newDeckId,
            "deckImage" to deckItem.deckImage,
            "deckTitle" to deckItem.deckTitle,
            "isPublic" to deckItem.isPublic,
            "userId" to userId, // Assign current user as the owner
            "cardItems" to deckItem.cardItems.map { card ->
                mapOf(
                    "question" to card.question,
                    "answer" to card.answer
                )
            } // Embed cards as an array
        )

        // Write the deck document to Firestore
        firestore.collection("deck_items")
            .document(newDeckId)
            .set(deckData)
            .addOnSuccessListener {
                Toast.makeText(this, "Deck '${deckItem.deckTitle}' imported successfully!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to import deck: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun openCardViewerActivity() {
        if (deckItem.cardItems.isEmpty()) {
            Toast.makeText(this, "No cards available to view!", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, CardViewerActivity::class.java)
        intent.putParcelableArrayListExtra("card_items", deckItem.cardItems)
        startActivity(intent)
    }
}
