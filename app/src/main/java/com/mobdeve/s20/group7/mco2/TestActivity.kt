package com.mobdeve.s20.group7.mco2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s20.group7.mco2.models.DeckItem

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
        
        val adapter = DeckAdapter(
            clickListener = TODO(),
            deckItems = TODO()
        )
        rvDecks.layoutManager = GridLayoutManager(this, 3)
        rvDecks.adapter = adapter
    }


}