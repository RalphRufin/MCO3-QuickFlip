package com.mobdeve.s20.group7.mco2

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.animation.addListener
import com.mobdeve.s20.group7.mco2.models.CardItem
import java.util.concurrent.TimeUnit

class CardTestActivity : AppCompatActivity() {

    private lateinit var cardContainer: CardView
    private lateinit var questionText: TextView
    private lateinit var timerText: TextView
    private lateinit var inputField: EditText
    private lateinit var nextButton: Button
    private lateinit var correctCounter: TextView

    private var cardItems: ArrayList<CardItem> = ArrayList()
    private var currentCardIndex = 0
    private var correctAnswers = 0
    private var isShowingQuestion = true
    private var shuffledIndices: List<Int> = listOf()

    private var isReviewingAnswer = false // Tracks if user is reviewing the answer

    private var elapsedTime = 0L // Timer elapsed time in seconds
    private lateinit var timerHandler: Handler
    private lateinit var timerRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cardtest)

        // Initialize views
        cardContainer = findViewById(R.id.cardContainer)
        questionText = findViewById(R.id.questionText)
        timerText = findViewById(R.id.timerText)
        inputField = findViewById(R.id.inputField)
        nextButton = findViewById(R.id.nextButton)
        correctCounter = findViewById(R.id.correctCounter)

        // Get card items from intent
        @Suppress("DEPRECATION")
        cardItems = intent.getParcelableArrayListExtra("card_items") ?: ArrayList()

        // Shuffle card indices
        shuffledIndices = cardItems.indices.shuffled()

        // Start the timer
        startTimer()

        // Show the first card
        showCard()

        nextButton.setOnClickListener {
            if (isReviewingAnswer) {
                proceedToNextCard() // Proceed to next card after review
            } else {
                checkAnswerAndProceed()
            }
        }
    }

    private fun startTimer() {
        elapsedTime = 0L
        timerHandler = Handler(Looper.getMainLooper())
        timerRunnable = object : Runnable {
            override fun run() {
                elapsedTime++
                timerText.text = formatElapsedTime(elapsedTime)
                timerHandler.postDelayed(this, 1000L) // Update every second
            }
        }
        timerHandler.post(timerRunnable)
    }

    private fun stopTimer() {
        timerHandler.removeCallbacks(timerRunnable)
    }

    private fun formatElapsedTime(seconds: Long): String {
        val minutes = TimeUnit.SECONDS.toMinutes(seconds)
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    private fun showCard() {
        if (currentCardIndex < shuffledIndices.size) {
            val currentCard = cardItems[shuffledIndices[currentCardIndex]]
            questionText.text = currentCard.question
            inputField.text.clear()
            isShowingQuestion = true
            resetCardAnimation()
        } else {
            stopTimer() // Stop the timer when the deck is completed
            nextButton.isEnabled = false
            showCompletionPopup() // Show final results
        }
        updateScoreDisplay()
    }

    private fun checkAnswerAndProceed() {
        if (currentCardIndex >= shuffledIndices.size) return

        val currentCard = cardItems[shuffledIndices[currentCardIndex]]
        val userAnswer = inputField.text.toString().trim()

        if (userAnswer.equals(currentCard.answer, ignoreCase = true)) {
            correctAnswers++
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Wrong! The correct answer was: ${currentCard.answer}", Toast.LENGTH_SHORT).show()
        }

        flipCardToShowAnswer(currentCard.answer)
    }

    private fun flipCardToShowAnswer(answer: String) {
        val cardFlip = ObjectAnimator.ofFloat(cardContainer, View.ROTATION_Y, 0f, 180f).apply {
            duration = 500
        }
        val textFlip = ObjectAnimator.ofFloat(questionText, View.ROTATION_Y, 0f, -180f).apply {
            duration = 500
        }

        cardFlip.start()
        textFlip.start()

        cardFlip.addListener(onEnd = {
            if (isShowingQuestion) {
                questionText.text = answer // Display the answer
            }
            isShowingQuestion = false
            prepareForAnswerReview() // Temporarily change button functionality
        })
    }

    private fun prepareForAnswerReview() {
        isReviewingAnswer = true
        nextButton.text = "Done Reviewing"
    }

    private fun proceedToNextCard() {
        currentCardIndex++
        isReviewingAnswer = false
        nextButton.text = "Next"
        showCard()
    }

    private fun resetCardAnimation() {
        cardContainer.rotationY = 0f
        questionText.rotationY = 0f // Reset text rotation
        isShowingQuestion = true
    }

    private fun updateScoreDisplay() {
        correctCounter.text = "Correct: $correctAnswers/${shuffledIndices.size}"
    }

    private fun showCompletionPopup() {
        val formattedTime = formatElapsedTime(elapsedTime)
        val message = "You completed the deck!\n\n" +
                "Final Time: $formattedTime\n" +
                "Correct Answers: $correctAnswers/${shuffledIndices.size}"

        AlertDialog.Builder(this)
            .setTitle("Test Completed")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
