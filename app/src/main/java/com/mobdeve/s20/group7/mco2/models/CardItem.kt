package com.mobdeve.s20.group7.mco2.models

data class CardItem(
    val question: String = "",  // Default values to prevent issues if Firestore doesn't provide them
    val answer: String = ""     // Default values to prevent issues if Firestore doesn't provide them
) {
    // No-argument constructor is implicitly added because of the default values
}