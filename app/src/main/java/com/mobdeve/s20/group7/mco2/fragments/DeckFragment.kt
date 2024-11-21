package com.mobdeve.s20.group7.mco2.fragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s20.group7.mco2.CardListActivity
import com.mobdeve.s20.group7.mco2.DeckAdapter
import com.mobdeve.s20.group7.mco2.R
import com.mobdeve.s20.group7.mco2.models.CardItem
import com.mobdeve.s20.group7.mco2.models.DeckItem
import com.mobdeve.s20.group7.mco2.session.UserSessionManager

class DeckFragment : Fragment() {
    private lateinit var userSessionManager: UserSessionManager
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var deckRecyclerView: RecyclerView
    private lateinit var deckItems: MutableList<DeckItem>
    private lateinit var deckImageView: ImageView
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var cardCountPicker: NumberPicker

    private var selectedImageUrl: String? = null
    private var maxDeckLimit: Int = 20 // Default limit

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

        // Initialize image picker launcher
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUrl = uri.toString()
                Glide.with(this)
                    .load(uri)
                    .into(deckImageView)
            }
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        userSessionManager = UserSessionManager(requireContext())

        deckRecyclerView = view.findViewById(R.id.rvDecks)
        deckRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        view.findViewById<View>(R.id.addDeckButton).setOnClickListener {
            fetchMaxDeckLimit()
        }

        loadDeckItems()
    }

    private fun fetchMaxDeckLimit() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    maxDeckLimit = document.getLong("maxDeckLimit")?.toInt() ?: 20
                    showAddDeckDialog()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error fetching max deck limit", e)
                    maxDeckLimit = 20
                    showAddDeckDialog()
                }
        } else {
            Log.e(TAG, "User ID not found")
            maxDeckLimit = 20
            showAddDeckDialog()
        }
    }

    private fun loadDeckItems() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("deck_items")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    deckItems = mutableListOf()
                    for (document in documents) {
                        val item = document.toObject(DeckItem::class.java)
                        item.id = document.id
                        deckItems.add(item)
                    }
                    setupAdapter()
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
            val context = requireContext()
            val intent = Intent(context, CardListActivity::class.java)
            intent.putExtra("deck_item", selectedDeck)
            context.startActivity(intent)

            // Update recent decks in Firestore
            updateRecentDecks(selectedDeck.id)
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

                // Remove the deck if it already exists to avoid duplicates
                currentRecentDecks.remove(deckId)

                // Add the new deck ID
                currentRecentDecks.add(deckId)

                // If more than 3 decks, remove the first (oldest) one
                if (currentRecentDecks.size > 3) {
                    currentRecentDecks.removeAt(0)
                }

                // Update the document with new recent decks
                transaction.update(userRef, "recentDecks", currentRecentDecks)
            }
                .addOnSuccessListener {
                    Log.d(TAG, "Recent decks updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error updating recent decks", e)
                }
        }
    }

    private fun showAddDeckDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_deck, null)

        val deckPublicSwitch = dialogView.findViewById<Switch>(R.id.deckPublicSwitch)
        val deckNameEditText = dialogView.findViewById<EditText>(R.id.deckNameEditText)
        deckImageView = dialogView.findViewById(R.id.deckImageView)
        val selectImageButton = dialogView.findViewById<Button>(R.id.selectImageButton)
        cardCountPicker = dialogView.findViewById(R.id.cardCountPicker)

        // Set the max value dynamically based on purchased deck limit
        cardCountPicker.minValue = 1
        cardCountPicker.maxValue = maxDeckLimit
        cardCountPicker.value = 1 // Default to minimum

        // Show a toast to inform user about the current deck limit
        Toast.makeText(requireContext(), "Current Deck Limit: $maxDeckLimit", Toast.LENGTH_SHORT).show()

        // Set click listener for the "Select Image" button
        selectImageButton.setOnClickListener {
            launchImagePicker()
        }

        builder.setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val deckName = deckNameEditText.text.toString().ifEmpty { "Deck ${deckItems.size + 1}" }
                val deckImage = selectedImageUrl ?: "quickflipcutedeck.png"
                val cardCount = cardCountPicker.value

                val cardItems = mutableListOf<CardItem>()
                for (i in 1..cardCount) {
                    cardItems.add(CardItem("Question $i", "Answer $i"))
                }

                val deckData = hashMapOf(
                    "userId" to auth.currentUser?.uid,
                    "deckImage" to deckImage,
                    "deckTitle" to deckName,
                    "cardItems" to cardItems,
                    "isPublic" to deckPublicSwitch.isChecked
                )

                // Add the deck to Firestore
                try {
                    firestore.collection("deck_items")
                        .add(deckData)
                        .addOnSuccessListener {
                            Log.d(TAG, "Deck added successfully")
                            loadDeckItems()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error adding deck", e)
                        }
                } catch (e: Exception) {
                    Log.e(TAG, "Error adding deck", e)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun launchImagePicker() {
        imagePickerLauncher.launch("image/*")
    }
}