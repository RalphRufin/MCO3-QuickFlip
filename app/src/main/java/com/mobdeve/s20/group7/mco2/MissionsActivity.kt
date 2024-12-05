package com.mobdeve.s20.group7.mco2

import MissionResetWorker
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.TimeZone
import androidx.work.*
import java.util.concurrent.TimeUnit



class MissionsActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var loginMissionCard: CardView
    private lateinit var test1DeckMissionCard: CardView
    private lateinit var test2DecksMissionCard: CardView
    private lateinit var claimRewardsButton: Button
    private lateinit var timerDisplay: TextView
    private var missionsCompleted = mutableMapOf<String, Boolean>()
    private var decksTestedToday: Long = 0
    private var clickCount = 0
    private var lastClickTime = 0L
    private val resetClickThreshold = 3 // Number of clicks required
    private val resetClickInterval = 1000L // Time interval in milliseconds (1 second)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_missions)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        initializeComponents()
        setupMissionCards()
        checkMissionStatus()
        observeMissionUpdates()
        observeUserPoints()
        startResetTimer()

        scheduleMissionResetNotification()

        // Add triple-click listener for testing purpose
        setupTripleClickReset()
    }

    private fun setupTripleClickReset() {
        timerDisplay.setOnClickListener {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastClickTime > resetClickInterval) {
                // Reset the counter if too much time has passed between clicks
                clickCount = 0
            }

            // Increment click count
            clickCount++
            lastClickTime = currentTime

            if (clickCount >= resetClickThreshold) {
                // Reset detected
                Toast.makeText(this, "Forcing daily reset...", Toast.LENGTH_SHORT).show()
                resetMissions()
                clickCount = 0 // Reset the counter
            }
        }
    }

    companion object {
        fun updateDeckTestMission(userId: String, deckCount: Long, callback: (Boolean, String) -> Unit) {
            val db = FirebaseFirestore.getInstance()
            val dailyRef = db.collection("users").document(userId).collection("missions").document("daily")

            dailyRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val decksTestedToday = document.getLong("decksTestedToday") ?: 0L
                    val newDecksTestedToday = decksTestedToday + deckCount

                    // Check if the mission is completed
                    val isTest1DeckCompleted = newDecksTestedToday >= 1
                    val isTest2DecksCompleted = newDecksTestedToday >= 2

                    val updates = mutableMapOf<String, Any>(
                        "decksTestedToday" to newDecksTestedToday
                    )

                    if (isTest1DeckCompleted) {
                        updates["test1Deck"] = true
                    }
                    if (isTest2DecksCompleted) {
                        updates["test2Decks"] = true
                    }

                    dailyRef.update(updates)
                        .addOnSuccessListener {
                            callback(true, "Mission updated successfully!")
                        }
                        .addOnFailureListener { e ->
                            callback(false, "Failed to update mission: ${e.message}")
                        }
                } else {
                    callback(false, "Mission data not found.")
                }
            }.addOnFailureListener { e ->
                callback(false, "Error fetching mission data: ${e.message}")
            }
        }
    }


    private fun initializeComponents() {
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loginMissionCard = findViewById(R.id.loginMissionCard)
        test1DeckMissionCard = findViewById(R.id.test1DeckMissionCard)
        test2DecksMissionCard = findViewById(R.id.test2DecksMissionCard)
        claimRewardsButton = findViewById(R.id.claimRewardsButton)
        timerDisplay = findViewById(R.id.timerDisplay)
    }

    private fun startResetTimer() {
        val nextResetTime = getNext3AMTimestamp()

        timerDisplay.postDelayed(object : Runnable {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                val remainingTime = nextResetTime - currentTime

                if (remainingTime > 0) {
                    val hours = (remainingTime / (1000 * 60 * 60)) % 24
                    val minutes = (remainingTime / (1000 * 60)) % 60
                    val seconds = (remainingTime / 1000) % 60

                    timerDisplay.text = String.format("Resets in: %02d:%02d:%02d", hours, minutes, seconds)
                    timerDisplay.postDelayed(this, 1000)
                } else {
                    timerDisplay.text = "Resetting..."
                    checkMissionStatus() // Use checkMissionStatus instead of direct reset
                }
            }
        }, 1000)
    }

    private fun getNext3AMTimestamp(): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"))
        calendar.add(Calendar.DAY_OF_MONTH, 1) // Move to next day
        calendar.set(Calendar.HOUR_OF_DAY, 3) // Set time to 3 AM
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun checkMissionStatus() {
        val userId = auth.currentUser?.uid ?: return
        val dailyRef = db.collection("users").document(userId).collection("missions").document("daily")

        dailyRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Safely get the last reset timestamp
                val lastResetTimestamp = try {
                    document.getTimestamp("lastResetTime")?.toDate()?.time ?: 0L
                } catch (e: Exception) {
                    // If timestamp conversion fails, default to 0
                    Log.e("MissionsActivity", "Error parsing last reset timestamp: ${e.message}")
                    0L
                }

                val currentTime = System.currentTimeMillis()
                val nextResetTime = getNext3AMTimestamp()

                // If document exists but lastResetTime is invalid, initialize missions
                if (lastResetTimestamp <= 0) {
                    initializeMissionsData()
                    return@addOnSuccessListener
                }

                // Only reset if we've passed the next reset time
                if (currentTime >= nextResetTime) {
                    // Prevent multiple resets by checking if we haven't already reset
                    if (lastResetTimestamp < nextResetTime) {
                        resetMissions()
                    } else {
                        // Load existing missions if already reset
                        document.data?.let { loadMissions(it) }
                    }
                } else {
                    // Load existing missions if not time to reset
                    document.data?.let { loadMissions(it) }
                }
            } else {
                // If document doesn't exist, initialize missions data
                initializeMissionsData()
            }
        }.addOnFailureListener { e ->
            Log.e("MissionsActivity", "Failed to fetch mission status: ${e.message}")
            // As a fallback, try to initialize missions
            initializeMissionsData()
        }
    }

    private fun resetMissions() {
        val userId = auth.currentUser?.uid ?: return
        val dailyRef = db.collection("users").document(userId).collection("missions").document("daily")

        val newMissions = mapOf(
            "login" to true,
            "test1Deck" to false,
            "test2Decks" to false,
            "decksTestedToday" to 0L,
            "lastResetTime" to FieldValue.serverTimestamp(),
            "rewardsClaimed" to false
        )

        dailyRef.set(newMissions).addOnSuccessListener {
            Toast.makeText(this, "Daily missions reset!", Toast.LENGTH_SHORT).show()
            loadMissions(newMissions) // Use loadMissions to update UI
            claimRewardsButton.isEnabled = false
        }.addOnFailureListener { e ->
            Log.e("MissionsActivity", "Error resetting missions: ${e.message}")
        }
    }

    private fun loadMissions(data: Map<String, Any?>) {
        missionsCompleted = data.filterKeys {
            it != "lastResetTime" && it != "decksTestedToday" && it != "rewardsClaimed"
        }.mapValues { it.value as Boolean }.toMutableMap()

        decksTestedToday = data["decksTestedToday"] as? Long ?: 0L

        // Check the actual rewards claimed status from Firestore
        val rewardsClaimed = data["rewardsClaimed"] as? Boolean ?: false

        // Only update UI if rewards haven't been claimed
        if (!rewardsClaimed) {
            updateAllMissionsUI()
        }
    }


    private fun observeUserPoints() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("MissionsActivity", "Failed to listen for points.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val points = snapshot.getLong("points") ?: 0
                findViewById<TextView>(R.id.pointsDisplay).text = "Points: $points"
            }
        }
    }

    private fun observeMissionUpdates() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("missions").document("daily")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("MissionsActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    loadMissions(snapshot.data!!)
                }
            }
    }

    private fun setupMissionCards() {
        missionsCompleted["login"] = true
        updateMissionUI(loginMissionCard, true)

        claimRewardsButton.setOnClickListener {
            if (canClaimRewards()) {
                claimRewards()
            } else {
                Toast.makeText(this, "Complete all missions first!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun canClaimRewards(): Boolean {
        // Check if all missions are completed and rewards have not been claimed
        val allMissionsCompleted = missionsCompleted.values.all { it }
        return allMissionsCompleted
    }

    private fun claimRewards() {
        val userId = auth.currentUser?.uid ?: return
        val dailyRef = db.collection("users").document(userId).collection("missions").document("daily")

        dailyRef.get().addOnSuccessListener { document ->
            val rewardsClaimed = document.getBoolean("rewardsClaimed") ?: false
            if (rewardsClaimed) {
                Toast.makeText(this, "Rewards already claimed for this cycle.", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            // Update user points and mark rewards as claimed
            val userRef = db.collection("users").document(userId)

            db.runTransaction { transaction ->
                // Increment points in the user document
                transaction.update(userRef, "points", FieldValue.increment(60))

                // Mark rewards as claimed in the daily missions document
                transaction.update(dailyRef, "rewardsClaimed", true)
            }.addOnSuccessListener {
                Toast.makeText(this, "Claimed 60 points!", Toast.LENGTH_SHORT).show()
                claimRewardsButton.isEnabled = false
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to claim rewards: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initializeMissionsData() {
        val userId = auth.currentUser?.uid ?: return

        val dailyRef = db.collection("users").document(userId).collection("missions").document("daily")

        val initialData = mapOf(
            "login" to true,
            "test1Deck" to false,
            "test2Decks" to false,
            "decksTestedToday" to 0L,
            "lastResetTime" to FieldValue.serverTimestamp(), // Ensures a proper timestamp is set
            "rewardsClaimed" to false
        )

        dailyRef.set(initialData)
            .addOnSuccessListener {
                Toast.makeText(this, "Daily missions initialized!", Toast.LENGTH_SHORT).show()
                updateAllMissionsUI()
            }
            .addOnFailureListener { e ->
                Log.e("MissionsActivity", "Error initializing missions: ${e.message}")
                // Optionally show an error to the user
                Toast.makeText(this, "Failed to initialize missions", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updateAllMissionsUI() {
        updateMissionUI(loginMissionCard, missionsCompleted["login"] ?: false)
        updateMissionUI(test1DeckMissionCard, missionsCompleted["test1Deck"] ?: false)
        updateMissionUI(test2DecksMissionCard, missionsCompleted["test2Decks"] ?: false)

        // Enable the claim button if all missions are completed
        val allMissionsCompleted = missionsCompleted.values.all { it }
        claimRewardsButton.isEnabled = allMissionsCompleted

        if (claimRewardsButton.isEnabled) {
            // Button is enabled, change to your desired color
            claimRewardsButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.rewards_claimable)
        } else {
            claimRewardsButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.light_gray)
        }
    }

    private fun updateMissionUI(card: CardView, completed: Boolean) {
        card.setCardBackgroundColor(
            if (completed) ContextCompat.getColor(this, R.color.mission_completed)
            else ContextCompat.getColor(this, R.color.mission_incomplete)
        )
    }

    private fun scheduleMissionResetNotification() {
        val currentTime = System.currentTimeMillis()
        val nextResetTime = getNext3AMTimestamp()
        val delayInMillis = nextResetTime - currentTime

        val workRequest = OneTimeWorkRequestBuilder<MissionResetWorker>()
                .setInitialDelay(nextResetTime, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }

}
