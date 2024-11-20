package com.mobdeve.s20.group7.mco2

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class MissionsActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var loginMissionCard: CardView
    private lateinit var test1DeckMissionCard: CardView
    private lateinit var test2DecksMissionCard: CardView
    private lateinit var claimRewardsButton: Button
    private var missionsCompleted = mutableMapOf<String, Boolean>()
    private var lastResetTime: Long = 0
    private var decksTestedToday: Long = 0

    companion object {
        fun updateDeckTestMission(userId: String, decksTested: Int) {
            val db = FirebaseFirestore.getInstance()
            val dailyRef = db.collection("users").document(userId)
                .collection("missions")
                .document("daily")

            dailyRef.get()
                .addOnSuccessListener { document ->
                    if (!document.exists()) {
                        val initialData = mapOf(
                            "login" to true,
                            "test1Deck" to false,
                            "test2Decks" to false,
                            "decksTestedToday" to 0L,
                            "lastResetTime" to System.currentTimeMillis()
                        )
                        dailyRef.set(initialData)
                            .addOnSuccessListener { performUpdate(dailyRef, decksTested) }
                            .addOnFailureListener { e ->
                                Log.e("MissionsActivity", "Error initializing missions: ${e.message}")
                            }
                    } else {
                        performUpdate(dailyRef, decksTested)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MissionsActivity", "Error fetching missions: ${e.message}")
                }
        }

        private fun performUpdate(dailyRef: DocumentReference, decksTested: Int) {
            dailyRef.get().addOnSuccessListener { document ->
                val currentTestCount = document.getLong("decksTestedToday") ?: 0
                val newTestCount = currentTestCount + decksTested

                val updates = mutableMapOf<String, Any>(
                    "decksTestedToday" to newTestCount,
                    "test1Deck" to (newTestCount >= 1),
                    "test2Decks" to (newTestCount >= 2)
                )

                dailyRef.update(updates)
                    .addOnSuccessListener {
                        Log.d("MissionsActivity", "Missions updated: decksTestedToday=$newTestCount")
                    }
                    .addOnFailureListener { e ->
                        Log.e("MissionsActivity", "Error updating missions: ${e.message}")
                    }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_missions)

        initializeComponents()
        setupMissionCards()
        checkMissionStatus()
        observeMissionUpdates()
        observeUserPoints()
        startResetTimer()
    }

    private fun startResetTimer() {
        val timerDisplay = findViewById<TextView>(R.id.timerDisplay)

        // Calculate the time remaining until midnight
        val currentTime = System.currentTimeMillis()
        val nextMidnight = getNextMidnightTime()
        val timeUntilMidnight = nextMidnight - currentTime

        // Create a countdown timer
        val timer = object : CountDownTimer(timeUntilMidnight, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = (millisUntilFinished / (1000 * 60 * 60)) % 24
                val minutes = (millisUntilFinished / (1000 * 60)) % 60
                val seconds = (millisUntilFinished / 1000) % 60

                timerDisplay.text = String.format("Resets in: %02d:%02d:%02d", hours, minutes, seconds)
            }

            override fun onFinish() {
                timerDisplay.text = "Resetting..."
                resetMissions()
            }
        }

        timer.start()
    }

    // Helper method to calculate the next midnight time
    private fun getNextMidnightTime(): Long {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = now
            add(java.util.Calendar.DAY_OF_MONTH, 1) // Move to the next day
            set(java.util.Calendar.HOUR_OF_DAY, 0) // Set to 12:00 AM
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }


    private fun observeUserPoints() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("MissionsActivity", "Failed to listen for points.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val points = snapshot.getLong("points") ?: 0
                    val pointsDisplay = findViewById<TextView>(R.id.pointsDisplay)
                    pointsDisplay.text = "Points: $points"
                }
            }
    }

    private fun observeMissionUpdates() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .collection("missions")
            .document("daily")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("MissionsActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    missionsCompleted = snapshot.data?.filterKeys {
                        it != "lastResetTime" && it != "decksTestedToday"
                    }?.mapValues { it.value as Boolean }?.toMutableMap() ?: mutableMapOf()

                    decksTestedToday = snapshot.getLong("decksTestedToday") ?: 0
                    updateAllMissionsUI()
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

        // For testing: Reset every 5 minutes
        checkAndResetMissions()
    }

    private fun setupMissionCards() {
        // Login mission is completed when activity starts since user must be logged in
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

    private fun checkMissionStatus() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .collection("missions")
            .document("daily")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    missionsCompleted = document.data?.filterKeys {
                        it != "lastResetTime" && it != "decksTestedToday"
                    }?.mapValues { it.value as Boolean }?.toMutableMap() ?: mutableMapOf()

                    lastResetTime = document.getLong("lastResetTime") ?: System.currentTimeMillis()
                    decksTestedToday = document.getLong("decksTestedToday") ?: 0

                    updateAllMissionsUI()
                } else {
                    initializeMissionsData()
                }
            }
    }

    private fun initializeMissionsData() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .collection("missions")
            .document("daily")
            .get()
            .addOnSuccessListener { document ->
                val currentDecksTested = document.getLong("decksTestedToday") ?: 0

                missionsCompleted = mutableMapOf(
                    "login" to true,
                    "test1Deck" to false,
                    "test2Decks" to false
                )
                lastResetTime = System.currentTimeMillis()

                db.collection("users").document(userId)
                    .collection("missions")
                    .document("daily")
                    .set(mapOf(
                        "login" to true,
                        "test1Deck" to (currentDecksTested >= 1),
                        "test2Decks" to (currentDecksTested >= 2),
                        "decksTestedToday" to currentDecksTested,
                        "lastResetTime" to lastResetTime
                    ))
                    .addOnSuccessListener {
                        updateAllMissionsUI()
                    }
                    .addOnFailureListener { e ->
                        Log.e("MissionsActivity", "Error initializing missions: ${e.message}")
                    }
            }
    }


    private fun checkAndResetMissions() {
        val currentTime = System.currentTimeMillis()
        // 5 minutes in milliseconds for testing
        if (currentTime - lastResetTime >= 5 * 60 * 1000) {
            resetMissions()
        }
    }

    private fun resetMissions() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .collection("missions")
            .document("daily")
            .get()
            .addOnSuccessListener { document ->
                val currentDecksTested = document.getLong("decksTestedToday") ?: 0

                missionsCompleted = mutableMapOf(
                    "login" to true,
                    "test1Deck" to (currentDecksTested >= 1),
                    "test2Decks" to (currentDecksTested >= 2)
                )

                db.collection("users").document(userId)
                    .collection("missions")
                    .document("daily")
                    .set(mapOf(
                        "login" to true,
                        "test1Deck" to (currentDecksTested >= 1),
                        "test2Decks" to (currentDecksTested >= 2),
                        "decksTestedToday" to currentDecksTested,
                        "rewardsClaimed" to false // Reset rewards claimed flag
                    ))
                    .addOnSuccessListener {
                        updateAllMissionsUI()
                        startResetTimer() // Restart the timer
                    }
                    .addOnFailureListener { e ->
                        Log.e("MissionsActivity", "Error resetting missions: ${e.message}")
                    }
            }
    }

    private fun updateAllMissionsUI() {
        updateMissionUI(loginMissionCard, missionsCompleted["login"] ?: false)
        updateMissionUI(test1DeckMissionCard, missionsCompleted["test1Deck"] ?: false)
        updateMissionUI(test2DecksMissionCard, missionsCompleted["test2Decks"] ?: false)

        // Check rewardsClaimed status to enable/disable the button
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .collection("missions")
            .document("daily")
            .get()
            .addOnSuccessListener { document ->
                val rewardsClaimed = document.getBoolean("rewardsClaimed") ?: false
                claimRewardsButton.isEnabled = !rewardsClaimed && canClaimRewards()
            }

        claimRewardsButton.isEnabled = canClaimRewards()
    }


    private fun updateMissionUI(card: CardView, completed: Boolean) {
        card.setCardBackgroundColor(
            if (completed)
                ContextCompat.getColor(this, R.color.mission_completed)
            else
                ContextCompat.getColor(this, R.color.mission_incomplete)
        )
    }

    private fun canClaimRewards(): Boolean {
        return missionsCompleted.values.all { it }
    }

    private fun claimRewards() {
        val userId = auth.currentUser?.uid ?: return

        val dailyRef = db.collection("users").document(userId)
            .collection("missions")
            .document("daily")

        dailyRef.get().addOnSuccessListener { document ->
            val rewardsClaimed = document.getBoolean("rewardsClaimed") ?: false

            if (rewardsClaimed) {
                Toast.makeText(this, "Rewards already claimed for this cycle.", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            // Update Firestore to increment points and set rewardsClaimed to true
            dailyRef.update(
                mapOf(
                    "rewardsClaimed" to true,
                    "points" to FieldValue.increment(60)
                )
            ).addOnSuccessListener {
                Toast.makeText(this, "Claimed 60 points!", Toast.LENGTH_SHORT).show()
                claimRewardsButton.isEnabled = false // Disable the button
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to claim rewards", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        checkMissionStatus() // Refresh mission status when activity comes to foreground
    }
}