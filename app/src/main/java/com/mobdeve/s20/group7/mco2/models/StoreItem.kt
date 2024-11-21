package com.mobdeve.s20.group7.mco2.models

data class StoreItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Int = 0,
    val iconResId: Int = 0,
    var isPurchased: Boolean = false
)