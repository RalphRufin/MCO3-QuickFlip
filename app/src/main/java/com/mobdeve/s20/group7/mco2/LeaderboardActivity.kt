package com.mobdeve.s20.group7.mco2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton

class LeaderboardActivity : BaseActivity() {

    private lateinit var btnShare: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)
        setupBaseComponents()

        btnShare = findViewById(R.id.btnShare)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        btnShare.setOnClickListener {
            val intent = Intent(this, SpeedTActivity::class.java)
            startActivity(intent)
        }
    }
}