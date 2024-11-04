package com.mobdeve.s20.group7.mco2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TestActivity : BaseActivity() {

    private lateinit var btnShare: ImageButton
    private lateinit var rvDecks: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test)

        setupBaseComponents()

        btnShare = findViewById(R.id.playBtn)
        rvDecks = findViewById(R.id.rvDecks)

        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        btnShare.setOnClickListener {
            val intent = Intent(this, SpeedTActivity::class.java)
            startActivity(intent)
        }
    }
    private fun setupRecyclerView() {
        val deckItems = createSampleDeckItems()
        val adapter = DeckAdapter(deckItems)
        rvDecks.layoutManager = GridLayoutManager(this, 3)
        rvDecks.adapter = adapter
    }

    private fun createSampleDeckItems(): List<DeckItem> {
        return listOf(
            DeckItem("@drawable/quickflipdeckicon", "Deck 1"),
            DeckItem("@drawable/quickflipdeckicon", "Deck 2"),
            DeckItem("@drawable/quickflipdeckicon", "Deck 3")
        )
    }
}