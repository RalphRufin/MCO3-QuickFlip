// FirebaseHelper.kt
package com.mobdeve.s20.group7.mco2.utils

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseHelper private constructor() {

    val db = FirebaseFirestore.getInstance()

    fun createNewUser(
        uid: String,
        username: String,
        email: String,
        authMethod: String,
        profilePicUrl: String = ""
    ): Task<Void> {
        val userData = hashMapOf(
            "username" to username,
            "email" to email,
            "authMethod" to authMethod,
            "profilePicUrl" to profilePicUrl
        )

        return db.collection("users").document(uid).set(userData)
    }

    companion object {
        private var instance: FirebaseHelper? = null

        fun getInstance(): FirebaseHelper {
            if (instance == null) {
                instance = FirebaseHelper()
            }
            return instance!!
        }
    }
}
