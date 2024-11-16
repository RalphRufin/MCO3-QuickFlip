package com.mobdeve.s20.group7.mco2

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s20.group7.mco2.models.CardItem
import com.mobdeve.s20.group7.mco2.models.DeckItem

class CardListActivity : AppCompatActivity() {

    private lateinit var rvCards: RecyclerView
    private lateinit var cardAdapter: CardAdapter
    private var cardItems: ArrayList<CardItem> = ArrayList()


    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private var selectedImageUrl: String? = null
    private lateinit var currentDeckImageView: ImageView

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var deckItem: DeckItem  // Store the current deck item
    private var deckId: String? = null      // Store the Firestore ID of the current deck

    companion object {
        private const val DEFAULT_IMAGE_URL = "quickflipcutedeck.png"  // Your default image
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_list)

        // Initialize Firestore and Auth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Get DeckItem from intent
        deckItem = intent.getParcelableExtra("deck_item") ?: DeckItem("", "", "", ArrayList<CardItem>(), false)
        deckId = deckItem.id // Assuming DeckItem has an ID field for Firestore document ID
        cardItems = deckItem.cardItems as ArrayList<CardItem>

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                selectedImageUrl = uri.toString()
                Glide.with(this)
                    .load(uri)
                    .into(currentDeckImageView)
            }
        }

        rvCards = findViewById(R.id.rvCards)
        rvCards.layoutManager = LinearLayoutManager(this)

        // Set up adapter
        cardAdapter = CardAdapter(cardItems)
        rvCards.adapter = cardAdapter

        // Set up Edit Deck button
        findViewById<Button>(R.id.btnEditDeck).setOnClickListener {
            showEditDeckDialog()
        }
    }

    private fun showEditDeckDialog() {
        if (deckId.isNullOrEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Unable to edit this deck. Deck ID is missing.")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
            return
        }

        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_deck, null)

        val deckNameEditText = dialogView.findViewById<EditText>(R.id.deckNameEditText)
        currentDeckImageView = dialogView.findViewById(R.id.deckImageView)
        val selectImageButton = dialogView.findViewById<Button>(R.id.selectImageButton)
        val cardCountPicker = dialogView.findViewById<NumberPicker>(R.id.cardCountPicker)
        val deckPublicSwitch = dialogView.findViewById<Switch>(R.id.deckPublicSwitch)
        val deleteDeckButton = dialogView.findViewById<Button>(R.id.deleteDeckButton)

        // Pre-fill dialog with current deck data
        deckNameEditText.setText(deckItem.deckTitle)
        Glide.with(this)
            .load(deckItem.deckImage)
            .error(Glide.with(this).load(DEFAULT_IMAGE_URL))
            .into(currentDeckImageView)

        cardCountPicker.minValue = 1
        cardCountPicker.maxValue = 20
        cardCountPicker.value = cardItems.size
        deckPublicSwitch.isChecked = deckItem.isPublic

        selectedImageUrl = deckItem.deckImage

        // Set up delete button
        deleteDeckButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Deck")
                .setMessage("Are you sure you want to delete this deck? This action cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    firestore.collection("deck_items").document(deckId!!)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Deck deleted successfully", Toast.LENGTH_SHORT).show()
                            finish() // Close the activity after deletion
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error deleting deck: ${e.message}", Toast.LENGTH_LONG).show()
                            Log.e("CardListActivity", "Error deleting deck", e)
                        }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Set up "Select Image" button
        selectImageButton.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        builder.setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val updatedDeckName = deckNameEditText.text.toString().ifEmpty { deckItem.deckTitle }
                val updatedCardCount = cardCountPicker.value
                val updatedIsPublic = deckPublicSwitch.isChecked

                if (updatedCardCount > cardItems.size) {
                    for (i in cardItems.size + 1..updatedCardCount) {
                        cardItems.add(CardItem("Question $i", "Answer $i"))
                    }
                } else if (updatedCardCount < cardItems.size) {
                    cardItems.subList(updatedCardCount, cardItems.size).clear()
                }

                val updatedDeckData = mapOf(
                    "deckTitle" to updatedDeckName,
                    "deckImage" to selectedImageUrl,
                    "cardItems" to cardItems,
                    "isPublic" to updatedIsPublic
                )

                firestore.collection("deck_items").document(deckId!!)
                    .update(updatedDeckData)
                    .addOnSuccessListener {
                        Log.d("CardListActivity", "Deck updated successfully")
                        deckItem.deckTitle = updatedDeckName
                        deckItem.deckImage = selectedImageUrl ?: DEFAULT_IMAGE_URL
                        deckItem.isPublic = updatedIsPublic
                        cardAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { e ->
                        Log.e("CardListActivity", "Error updating deck", e)
                    }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


}
