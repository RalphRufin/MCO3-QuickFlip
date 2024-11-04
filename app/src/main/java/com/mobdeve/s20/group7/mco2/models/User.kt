package com.mobdeve.s20.group7.mco2.models

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val authMethod: String = "",
    val profilePicUrl: String = "",
    val joinDate: Long = 0,
    val stats: UserStats = UserStats()
)

data class UserStats(
    val totalCards: Int = 0,
    val cardsReviewed: Int = 0,
    val averageSpeed: Double = 0.0,
    val ranking: Int = 0
)