package com.mobdeve.s20.group7.mco2

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ViewFlipper
import androidx.cardview.widget.CardView
import com.mobdeve.s20.group7.mco2.BaseActivity
import com.mobdeve.s20.group7.mco2.R

class SpeedTActivity : BaseActivity() {
    private lateinit var viewFlipper: ViewFlipper
    private var isShowingQuestion = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speedt)
        setupBaseComponents()
        viewFlipper = findViewById(R.id.viewFlipper)

        val inAnimation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        val outAnimation = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right)

        viewFlipper.inAnimation = inAnimation
        viewFlipper.outAnimation = outAnimation

        findViewById<CardView>(R.id.cardContainer).setOnClickListener {
            if (isShowingQuestion) {
                viewFlipper.showNext()
            } else {
                viewFlipper.showPrevious()
            }
            isShowingQuestion = !isShowingQuestion
        }
    }
}