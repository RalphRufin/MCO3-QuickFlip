package com.mobdeve.s20.group7.mco2

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProfileActivity : BaseActivity() {

    private lateinit var rvStoreItems: RecyclerView
    private lateinit var storeAdapter: StoreAdapter
    private val storeItems = mutableListOf<StoreItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setupBaseComponents()

        rvStoreItems = findViewById(R.id.rvStoreItems)

        rvStoreItems.layoutManager = GridLayoutManager(this, 3)
        loadStoreItems()

        storeAdapter = StoreAdapter(this, storeItems)
        rvStoreItems.adapter = storeAdapter
    }

    private fun loadStoreItems() {
        storeItems.add(StoreItem("Dark Mode", 200, R.drawable.quickflipnightmodeicon))
        storeItems.add(StoreItem("More Stamina", 300, R.drawable.quickflipbatteryicon))
        storeItems.add(StoreItem("More Decks", 100, R.drawable.quickflipmoredecksicon))
    }
}
