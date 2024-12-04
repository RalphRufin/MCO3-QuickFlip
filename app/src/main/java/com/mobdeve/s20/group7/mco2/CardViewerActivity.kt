package com.mobdeve.s20.group7.mco2

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.mobdeve.s20.group7.mco2.models.CardItem
import kotlin.math.atan2
import kotlin.random.Random

class CardViewerActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var cardContainer: CardView
    private lateinit var textDisplay: TextView
    private lateinit var gestureDetector: GestureDetector
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor

    private var cardItems: ArrayList<CardItem> = ArrayList()
    private var currentCardIndex = 0
    private var isShowingQuestion = true
    private var lastCircularX = 0f
    private var lastCircularY = 0f
    private var startAngle = 0f
    private var isRandomized = false
    private var randomizedIndices: List<Int> = listOf()

    private var lastShakeTime: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var lastUpdate: Long = 0
    private val SHAKE_THRESHOLD = 800
    private val SWIPE_THRESHOLD = 100f
    private var initialTouchY = 0f
    private var lastTouchY = 0f
    private var isMultiTouch = false
    private var isCircularMotion = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cardviewer)

        // Initialize views
        cardContainer = findViewById(R.id.cardContainer)
        textDisplay = findViewById(R.id.textDisplay)

        // Get card items from intent
        @Suppress("DEPRECATION")
        cardItems = intent.getParcelableArrayListExtra("card_items") ?: ArrayList()

        // Initialize sensor manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!

        // Set up gesture detector
        setupGestureDetector()

        // Display first card
        updateCardDisplay()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true
        })

        cardContainer.setOnTouchListener { _, event ->
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    isMultiTouch = false
                    isCircularMotion = false
                    lastCircularX = event.x
                    lastCircularY = event.y
                    startAngle = getAngle(event.x, event.y)
                    initialTouchY = event.y
                    lastTouchY = event.y
                    true
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    if (event.pointerCount == 2) {
                        isMultiTouch = true
                        isCircularMotion = false
                        initialTouchY = event.getY(0)
                        lastTouchY = event.getY(0)
                    }
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isMultiTouch && event.pointerCount == 2) {
                        // Handle two-finger swipe with reduced threshold
                        val currentY = event.getY(0)
                        val deltaY = currentY - lastTouchY
                        if (Math.abs(deltaY) > SWIPE_THRESHOLD) {
                            if (deltaY > 0) {
                                showPreviousCard()
                            } else {
                                showNextCard()
                            }
                            lastTouchY = currentY
                        }
                    } else if (!isMultiTouch) {
                        // Handle circular motion (unchanged)
                        val currentAngle = getAngle(event.x, event.y)
                        val deltaAngle = currentAngle - startAngle

                        if (Math.abs(deltaAngle) > 45) {
                            isCircularMotion = true
                            if (deltaAngle > 0) {
                                if (!isShowingQuestion) flipCard(true)
                            } else {
                                if (isShowingQuestion) flipCard(false)
                            }
                            startAngle = currentAngle
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    isMultiTouch = false
                    isCircularMotion = false
                    true
                }
                else -> false
            }
        }
    }

    private fun getAngle(x: Float, y: Float): Float {
        val centerX = cardContainer.width / 2f
        val centerY = cardContainer.height / 2f
        return Math.toDegrees(atan2((y - centerY).toDouble(), (x - centerX).toDouble())).toFloat()
    }

    private fun flipCard(toQuestion: Boolean) {
        cardContainer.animate()
            .rotationY(if (toQuestion) 0f else 180f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                isShowingQuestion = toQuestion
                updateCardDisplay()
            }
            .start()

        // Animate the text to keep it readable
        textDisplay.animate()
            .rotationY(if (toQuestion) 0f else -180f)  // Counter-rotate the text
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun showNextCard() {
        if (isRandomized) {
            val currentRandomIndex = randomizedIndices.indexOf(currentCardIndex)
            if (currentRandomIndex < randomizedIndices.size - 1) {
                currentCardIndex = randomizedIndices[currentRandomIndex + 1]
            }
        } else {
            if (currentCardIndex < cardItems.size - 1) {
                currentCardIndex++
            }
        }
        isShowingQuestion = true
        // Reset rotations when showing new card
        cardContainer.rotationY = 0f
        textDisplay.rotationY = 0f
        updateCardDisplay()
    }

    private fun showPreviousCard() {
        if (isRandomized) {
            val currentRandomIndex = randomizedIndices.indexOf(currentCardIndex)
            if (currentRandomIndex > 0) {
                currentCardIndex = randomizedIndices[currentRandomIndex - 1]
            }
        } else {
            if (currentCardIndex > 0) {
                currentCardIndex--
            }
        }
        isShowingQuestion = true
        // Reset rotations when showing new card
        cardContainer.rotationY = 0f
        textDisplay.rotationY = 0f
        updateCardDisplay()
    }

    private fun updateCardDisplay() {
        val currentCard = cardItems[currentCardIndex]
        textDisplay.text = if (isShowingQuestion) currentCard.question else currentCard.answer
    }

    private fun shuffleCards() {
        isRandomized = true
        randomizedIndices = cardItems.indices.shuffled()
        currentCardIndex = randomizedIndices[0]
        isShowingQuestion = true

        // Perform shuffle animation
        val shuffleAnimation = AnimatorSet()

        // Create scatter animation
        val scatterAnimations = ArrayList<ObjectAnimator>()

        // Scatter the card
        val scatterTranslationX = ObjectAnimator.ofFloat(cardContainer, View.TRANSLATION_X, 0f, 100f)
        val scatterTranslationY = ObjectAnimator.ofFloat(cardContainer, View.TRANSLATION_Y, 0f, -50f)
        val scatterRotation = ObjectAnimator.ofFloat(cardContainer, View.ROTATION, 0f, 360f)

        scatterAnimations.add(scatterTranslationX)
        scatterAnimations.add(scatterTranslationY)
        scatterAnimations.add(scatterRotation)

        // Create return animation
        val returnAnimations = ArrayList<ObjectAnimator>()

        // Return card to original position
        val returnTranslationX = ObjectAnimator.ofFloat(cardContainer, View.TRANSLATION_X, 100f, 0f)
        val returnTranslationY = ObjectAnimator.ofFloat(cardContainer, View.TRANSLATION_Y, -50f, 0f)
        val returnRotation = ObjectAnimator.ofFloat(cardContainer, View.ROTATION, 360f, 0f)

        returnAnimations.add(returnTranslationX)
        returnAnimations.add(returnTranslationY)
        returnAnimations.add(returnRotation)

        // Set up the scatter phase
        val scatterSet = AnimatorSet()
        scatterSet.playTogether(scatterAnimations as Collection<Animator>?)
        scatterSet.duration = 300
        scatterSet.interpolator = AccelerateDecelerateInterpolator()

        // Set up the return phase
        val returnSet = AnimatorSet()
        returnSet.playTogether(returnAnimations as Collection<Animator>?)
        returnSet.duration = 300
        returnSet.interpolator = AnticipateOvershootInterpolator()

        // Combine both phases
        shuffleAnimation.playSequentially(scatterSet, returnSet)

        // Start the animation and update the display when complete
        shuffleAnimation.start()
        shuffleAnimation.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                // Reset rotations and update display
                cardContainer.rotationY = 0f
                textDisplay.rotationY = 0f
                updateCardDisplay()

                // Show a toast to indicate shuffle is complete
                Toast.makeText(this@CardViewerActivity, "Cards Shuffled!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Rest of the sensor-related code remains the same
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val curTime = System.currentTimeMillis()
            if ((curTime - lastShakeTime) > 500) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val lastUpdateTime = lastUpdate
                val timeDiff = curTime - lastUpdateTime

                if (timeDiff > 100) {
                    val speed = Math.abs(x + y + z - lastX - lastY - lastZ) / timeDiff * 10000
                    if (speed > SHAKE_THRESHOLD) {
                        shuffleCards()
                        lastShakeTime = curTime
                    }
                    lastX = x
                    lastY = y
                    lastZ = z
                    lastUpdate = curTime
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}