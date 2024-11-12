package com.mobdeve.s20.group7.mco2.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s20.group7.mco2.DeckAdapter
import com.mobdeve.s20.group7.mco2.R
import com.mobdeve.s20.group7.mco2.models.DeckItem
import com.mobdeve.s20.group7.mco2.session.UserSessionManager

class DeckFragment : Fragment() {

    private lateinit var userSessionManager: UserSessionManager
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var deckRecyclerView: RecyclerView
    private lateinit var deckItems: MutableList<DeckItem>

    companion object {
        private const val TAG = "DeckFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_deck, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        userSessionManager = UserSessionManager(requireContext())

        deckRecyclerView = view.findViewById(R.id.rvDecks)
        deckRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        loadDeckItems()
    }

    private fun loadDeckItems() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("deck_items")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        createDefaultDecks(userId)
                    } else {
                        deckItems = mutableListOf()
                        for (document in documents) {
                            val item = document.toObject(DeckItem::class.java)
                            deckItems.add(item)
                        }
                        setupAdapter()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error loading deck items", e)
                }
        } else {
            Log.e(TAG, "User ID not found in session")
        }
    }

    private fun setupAdapter() {
        deckRecyclerView.adapter = DeckAdapter(deckItems) { selectedDeck ->
            Log.d(TAG, "Selected Deck: ${selectedDeck.deckTitle}")
            // Additional actions on selecting the deck, like updating a detail view, if desired
        }
    }

    private fun createDefaultDecks(userId: String) {
        val defaultDecks = listOf(
            DeckItem("default_image_1.png", "Default Deck 1"),
            DeckItem("default_image_2.png", "Default Deck 2")
        )

        for (deck in defaultDecks) {
            val deckData = hashMapOf(
                "userId" to userId,
                "deckImage" to deck.deckImage,
                "deckTitle" to deck.deckTitle,
                "cardItems" to deck.cardItems
            )

            firestore.collection("deck_items")
                .add(deckData)
                .addOnSuccessListener {
                    Log.d(TAG, "Default deck added successfully")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error adding default deck", e)
                }
        }

        loadDeckItems()
    }
}
