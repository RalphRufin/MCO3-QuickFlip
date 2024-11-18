package com.mobdeve.s20.group7.mco2.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s20.group7.mco2.CardListActivity
import com.mobdeve.s20.group7.mco2.DeckAdapter
import com.mobdeve.s20.group7.mco2.R
import com.mobdeve.s20.group7.mco2.models.DeckItem

class BrowseFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var deckAdapter: DeckAdapter
    private var deckList: ArrayList<DeckItem> = ArrayList()
    private var fullDeckList: ArrayList<DeckItem> = ArrayList() // Cache all public decks
    private lateinit var firestore: FirebaseFirestore

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_browse, container, false)

        firestore = FirebaseFirestore.getInstance()

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.deck_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3) // 2 columns
        deckAdapter = DeckAdapter(deckList) { deckItem ->
            navigateToCardList(deckItem)
        }
        recyclerView.adapter = deckAdapter

        // Setup Search Bar
        searchEditText = view.findViewById(R.id.searchEditText)
        searchButton = view.findViewById(R.id.searchButton)
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            filterDecks(query)
        }

        loadPublicDecks()
        return view
    }

    private fun loadPublicDecks() {
        firestore.collection("deck_items")
            .whereEqualTo("isPublic", true)
            .get()
            .addOnSuccessListener { querySnapshot ->
                fullDeckList.clear()
                for (document in querySnapshot.documents) {
                    val deck = document.toObject(DeckItem::class.java)
                    deck?.let {
                        it.id = document.id
                        fullDeckList.add(it)
                    }
                }
                deckList.clear()
                deckList.addAll(fullDeckList)
                deckAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("BrowseFragment", "Error loading decks: ${exception.message}")
                Toast.makeText(requireContext(), "Error loading decks", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterDecks(query: String) {
        deckList.clear()
        deckList.addAll(
            if (query.isEmpty()) fullDeckList
            else fullDeckList.filter { it.deckTitle.contains(query, ignoreCase = true) }
        )
        deckAdapter.notifyDataSetChanged()
    }

    private fun navigateToCardList(deckItem: DeckItem) {
        val intent = Intent(requireContext(), CardListActivity::class.java)
        intent.putExtra("deck_item", deckItem)
        startActivity(intent)
    }
}
