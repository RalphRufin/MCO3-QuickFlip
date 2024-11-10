package com.mobdeve.s20.group7.mco2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupBaseComponents()

        //instance
        auth = FirebaseAuth.getInstance()

        //basic check
        if (auth.currentUser == null) {
            navigateToLogin()
        }

        val deckButton: ImageButton = findViewById(R.id.deckButton)
        val browseButton: ImageButton = findViewById(R.id.browseButton)
        val storeButton: ImageButton = findViewById(R.id.storeButton)
        val testButton: ImageButton = findViewById(R.id.testButton)

        deckButton.setOnClickListener {
            val intent = Intent(this, DeckActivity::class.java)
            startActivity(intent)
        }

        browseButton.setOnClickListener {
            val intent = Intent(this, BrowseActivity::class.java)
            startActivity(intent)
            onBrowseButtonClick()
        }

        storeButton.setOnClickListener {
            val intent = Intent(this, StoreActivity::class.java)
            startActivity(intent)
        }

        testButton.setOnClickListener {
            val intent = Intent(this, TestActivity::class.java)
            startActivity(intent)
            onTestButtonClick()
        }


    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun onDeckButtonClick() {
        // TODO: Implement Deck activity launch
    }

    private fun onBrowseButtonClick() {
        // TODO: Implement Browse activity launch
    }

    private fun onStoreButtonClick() {
        // TODO: Implement Store activity launch
    }

    private fun onTestButtonClick() {
        // TODO: Implement Test activity launch
    }
}