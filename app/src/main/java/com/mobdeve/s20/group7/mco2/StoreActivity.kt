package com.mobdeve.s20.group7.mco2

import android.app.AlertDialog
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s20.group7.mco2.models.StoreItem

class StoreActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var rvStoreItems: RecyclerView
    private lateinit var tvUserPoints: TextView
    private lateinit var storeItemAdapter: StoreItemAdapter
    private var userPoints: Long = 0
    private var storeItems: MutableList<StoreItem> = mutableListOf()

    companion object {
        // Predefined store items with deck limit increase
        private fun createInitialStoreItems(): List<StoreItem> = listOf(
            StoreItem(
                id = "deck_limit_30",
                name = "Deck Limit: 30 Cards",
                description = "Increase your maximum deck size to 30 cards",
                price = 100,
                iconResId = R.drawable.quickflip30icon
            ),
            StoreItem(
                id = "deck_limit_40",
                name = "Deck Limit: 40 Cards",
                description = "Increase your maximum deck size to 40 cards",
                price = 200,
                iconResId = R.drawable.quickflip40icon
            ),
            StoreItem(
                id = "deck_limit_50",
                name = "Deck Limit: 50 Cards",
                description = "Increase your maximum deck size to 50 cards",
                price = 300,
                iconResId = R.drawable.quickflip50icon
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        initializeComponents()
        fetchUserData()
        initializeStoreItems()
    }

    private fun initializeComponents() {
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        rvStoreItems = findViewById(R.id.rvStoreItems)
        tvUserPoints = findViewById(R.id.pointsDisplay)
    }

    private fun fetchUserData() {
        val userId = auth.currentUser?.uid ?: return

        // Fetch user points and other data
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    userPoints = document.getLong("points") ?: 0
                    val maxDeckLimit = document.getLong("maxDeckLimit") ?: 20

                    tvUserPoints.text = "Points: $userPoints"
                    Toast.makeText(this, "Max Deck Limit: $maxDeckLimit", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun initializeStoreItems() {
        val userId = auth.currentUser?.uid ?: return

        // Fetch purchased items for the user
        db.collection("users").document(userId)
            .collection("purchasedItems")
            .get()
            .addOnSuccessListener { purchasedSnapshot ->
                val purchasedItemIds = purchasedSnapshot.documents.map { it.id }

                // Use predefined items and mark as purchased if already bought
                storeItems = createInitialStoreItems().map { item ->
                    item.copy(isPurchased = item.id in purchasedItemIds)
                }.toMutableList()

                setupRecyclerView()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load store items: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRecyclerView() {
        storeItemAdapter = StoreItemAdapter(storeItems) { item ->
            handleItemPurchase(item)
        }

        rvStoreItems.layoutManager = LinearLayoutManager(this)
        rvStoreItems.adapter = storeItemAdapter
    }

    private fun handleItemPurchase(item: StoreItem) {
        if (item.isPurchased) {
            Toast.makeText(this, "You have already purchased this item.", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate purchase prerequisites
        val prerequisiteId = when (item.id) {
            "deck_limit_40" -> "deck_limit_30"
            "deck_limit_50" -> "deck_limit_40"
            else -> null
        }

        if (prerequisiteId != null && storeItems.find { it.id == prerequisiteId }?.isPurchased != true) {
            AlertDialog.Builder(this)
                .setTitle("Purchase Locked")
                .setMessage("You must purchase '${storeItems.find { it.id == prerequisiteId }?.name}' before buying '${item.name}'.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        if (userPoints < item.price) {
            AlertDialog.Builder(this)
                .setTitle("Insufficient Points")
                .setMessage("You do not have enough points to purchase this item. \nRequired: ${item.price} points \nCurrent: $userPoints points")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Confirm Purchase")
            .setMessage("Do you want to buy '${item.name}' for ${item.price} points?")
            .setPositiveButton("Buy") { _, _ -> purchaseItem(item) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun purchaseItem(item: StoreItem) {
        val userId = auth.currentUser?.uid ?: return
        val batch = db.batch()

        // Reduce user points
        val userRef = db.collection("users").document(userId)
        batch.update(userRef, "points", userPoints - item.price)

        // Mark item as purchased
        val purchasedItemRef = userRef.collection("purchasedItems").document(item.id)
        batch.set(purchasedItemRef, mapOf(
            "purchasedAt" to System.currentTimeMillis(),
            "itemDetails" to mapOf(
                "name" to item.name,
                "description" to item.description
            )
        ))

        // Update user's max deck limit based on purchase
        val maxDeckLimit = when (item.id) {
            "deck_limit_30" -> 30
            "deck_limit_40" -> 40
            "deck_limit_50" -> 50
            else -> 20 // default
        }
        batch.update(userRef, "maxDeckLimit", maxDeckLimit)

        batch.commit()
            .addOnSuccessListener {
                userPoints -= item.price
                tvUserPoints.text = "Points: $userPoints"

                // Update local list and notify adapter
                val index = storeItems.indexOf(item)
                storeItems[index].isPurchased = true
                storeItemAdapter.notifyItemChanged(index)

                Toast.makeText(this, "Item purchased successfully! Deck limit increased.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Purchase failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }

}
