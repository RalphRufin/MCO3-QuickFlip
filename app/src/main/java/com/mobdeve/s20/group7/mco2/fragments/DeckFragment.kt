package com.mobdeve.s20.group7.mco2.fragments

import android.app.AlertDialog
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    private lateinit var deckImageView: ImageView  // This is the ImageView for showing the selected image
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>  // Image picker launcher

    private var selectedImageUrl: String? = null  // Store the selected image URL

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
                selectedImageUrl = uri.toString()  // Store the image URL
                Glide.with(this)  // Using Glide to load the image into the ImageView
                    .load(uri)
                    .into(deckImageView)
            }
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        userSessionManager = UserSessionManager(requireContext())

        deckRecyclerView = view.findViewById(R.id.rvDecks)
        deckRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        view.findViewById<View>(R.id.addDeckButton).setOnClickListener {
            showAddDeckDialog()  // Show the dialog to add a deck
        }

        loadDeckItems()  // Load deck items from Firestore
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
                        deckItems.add(item)
                    }
                    setupAdapter()  // Set up the RecyclerView adapter with loaded data
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
        }
    }

    private fun showAddDeckDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_deck, null)

        val deckNameEditText = dialogView.findViewById<EditText>(R.id.deckNameEditText)
        deckImageView = dialogView.findViewById(R.id.deckImageView)  // Use the class-level reference
        val selectImageButton = dialogView.findViewById<Button>(R.id.selectImageButton)
        val cardCountPicker = dialogView.findViewById<NumberPicker>(R.id.cardCountPicker)

        cardCountPicker.minValue = 1
        cardCountPicker.maxValue = 20

        // Set click listener for the "Select Image" button
        selectImageButton.setOnClickListener {
            launchImagePicker()  // Launch the image picker
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
                    "cardItems" to cardItems
                )

                // Add the deck to Firestore
                try {
                    firestore.collection("deck_items")
                        .add(deckData)
                        .addOnSuccessListener {
                            Log.d(TAG, "Deck added successfully")
                            loadDeckItems()  // Reload deck items
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
        imagePickerLauncher.launch("image/*")  // Launch image picker for any image type
    }
}
