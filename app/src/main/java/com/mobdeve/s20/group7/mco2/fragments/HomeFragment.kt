package com.mobdeve.s20.group7.mco2.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s20.group7.mco2.CardListActivity
import com.mobdeve.s20.group7.mco2.MissionsActivity
import com.mobdeve.s20.group7.mco2.R
import com.mobdeve.s20.group7.mco2.StoreActivity
import com.mobdeve.s20.group7.mco2.CircularRecyclerViewAdapter
import com.mobdeve.s20.group7.mco2.models.DeckItem

class HomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var recentDecksRecyclerView: RecyclerView
    private lateinit var recentDecks: MutableList<DeckItem>
    private lateinit var circularAdapter: CircularRecyclerViewAdapter

    companion object {
        private const val TAG = "HomeFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Set up buttons
        view.findViewById<ImageButton>(R.id.missionsButton).setOnClickListener {
            startActivity(Intent(requireContext(), MissionsActivity::class.java))
        }

        view.findViewById<ImageButton>(R.id.storeButton).setOnClickListener {
            startActivity(Intent(requireContext(), StoreActivity::class.java))
        }

        // Set up recent decks recycler view
        recentDecksRecyclerView = view.findViewById(R.id.rvRecentDecks)

        // Fetch and load recent decks
        fetchRecentDecks()

        return view
    }

    private fun fetchRecentDecks() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val recentDeckIds = document.get("recentDecks") as? List<String> ?: emptyList()

                    // Filter out non-existent deck IDs before fetching details
                    val validDeckIds = mutableListOf<String>()

                    // Create a chain of checks to handle async nature of Firestore
                    val checkDeckExistence = { deckId: String, continuation: (Boolean) -> Unit ->
                        firestore.collection("deck_items").document(deckId).get()
                            .addOnSuccessListener { snapshot ->
                                // Check if the document exists and is not empty
                                continuation(snapshot.exists() && snapshot.data != null)
                            }
                            .addOnFailureListener {
                                continuation(false)
                            }
                    }

                    // Check existence of each deck ID
                    fun checkNextDeck(index: Int) {
                        if (index < recentDeckIds.size) {
                            checkDeckExistence(recentDeckIds[index]) { exists ->
                                if (exists) {
                                    validDeckIds.add(recentDeckIds[index])
                                }

                                // Move to next deck
                                checkNextDeck(index + 1)
                            }
                        } else {
                            // All decks checked, proceed with fetching details
                            if (validDeckIds.isNotEmpty()) {
                                fetchDeckDetails(validDeckIds)
                            } else {
                                // No valid decks found
                                recentDecks = mutableListOf()
                                setupRecentDecksAdapter()
                            }
                        }
                    }

                    // Start the checking process
                    checkNextDeck(0)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error fetching recent decks", e)
                    recentDecks = mutableListOf()
                    setupRecentDecksAdapter()
                }
        }
    }

    private fun fetchDeckDetails(recentDeckIds: List<String>) {
        recentDecks = mutableListOf()

        // If no recent decks, return early
        if (recentDeckIds.isEmpty()) {
            setupRecentDecksAdapter()
            return
        }

        // Track the number of successful and failed fetches
        var successfulFetches = 0
        var failedFetches = 0

        // Fetch details for each recent deck
        recentDeckIds.forEach { deckId ->
            firestore.collection("deck_items").document(deckId).get()
                .addOnSuccessListener { document ->
                    val deckItem = document.toObject(DeckItem::class.java)
                    deckItem?.id = document.id
                    deckItem?.let {
                        recentDecks.add(it)
                        successfulFetches++
                    }

                    // Check if all fetches are complete
                    if (successfulFetches + failedFetches == recentDeckIds.size) {
                        setupRecentDecksAdapter()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error fetching deck details", e)
                    failedFetches++

                    // Check if all fetches are complete
                    if (successfulFetches + failedFetches == recentDeckIds.size) {
                        setupRecentDecksAdapter()
                    }
                }
        }
    }

    private fun setupRecentDecksAdapter() {
        // Add a check to handle empty decks list
        if (recentDecks.isEmpty()) {
            recentDecksRecyclerView.visibility = View.GONE
            return
        }

        // Ensure RecyclerView is visible
        recentDecksRecyclerView.visibility = View.VISIBLE

        // Create circular adapter
        circularAdapter = CircularRecyclerViewAdapter(recentDecks) { selectedDeck ->
            val intent = Intent(requireContext(), CardListActivity::class.java)
            intent.putExtra("deck_item", selectedDeck)
            startActivity(intent)

            // Update recent decks in Firestore
            updateRecentDecks(selectedDeck.id)
        }

        // Set up LinearLayoutManager with horizontal orientation
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recentDecksRecyclerView.layoutManager = layoutManager

        // Attach PagerSnapHelper for paging behavior
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recentDecksRecyclerView)

        // Set the adapter
        recentDecksRecyclerView.adapter = circularAdapter

        // Scroll behavior based on number of decks
        when {
            recentDecks.size == 1 -> {
                // No scrolling for single deck
                recentDecksRecyclerView.isNestedScrollingEnabled = false
            }
            recentDecks.size >= 2 -> {
                // Scroll to create an alternating view for multiple decks
                recentDecksRecyclerView.scrollToPosition(recentDecks.size)
            }
        }
    }

    private fun updateRecentDecks(deckId: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = firestore.collection("users").document(userId)

            firestore.runTransaction { transaction ->
                val userDocument = transaction.get(userRef)

                // Get current recent decks or initialize empty list
                val currentRecentDecks = userDocument.get("recentDecks") as? MutableList<String>
                    ?: mutableListOf()

                // Remove the deck if it exists
                currentRecentDecks.remove(deckId)

                // Add the new deck ID at the end
                currentRecentDecks.add(deckId)

                // If more than 3 decks, remove the first (oldest) one
                while (currentRecentDecks.size > 3) {
                    currentRecentDecks.removeAt(0)
                }

                // Update the document with new recent decks
                transaction.update(userRef, "recentDecks", currentRecentDecks)
            }
                .addOnSuccessListener {
                    Log.d(TAG, "Recent decks updated successfully")
                    // Refresh the recent decks after update
                    fetchRecentDecks()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error updating recent decks", e)
                }
        }
    }
}