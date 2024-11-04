package com.mobdeve.s20.group7.mco2.utils

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mobdeve.s20.group7.mco2.models.User
import com.mobdeve.s20.group7.mco2.models.UserStats

class FirebaseHelper {
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")

    fun createNewUser(
        uid: String,
        username: String,
        email: String,
        authMethod: String,
        profilePicUrl: String = ""
    ): Task<Void> {
        val user = User(
            uid = uid,
            username = username,
            email = email,
            authMethod = authMethod,
            profilePicUrl = profilePicUrl,
            joinDate = System.currentTimeMillis(),
            stats = UserStats()
        )

        return usersRef.child(uid).setValue(user)
    }

    fun getCurrentUser(callback: (User?) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            callback(null)
            return
        }

        usersRef.child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    callback(user)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }

    fun updateUserStats(
        uid: String,
        stats: UserStats
    ): Task<Void> {
        return usersRef.child(uid).child("stats").setValue(stats)
    }

    fun updateUsername(
        uid: String,
        newUsername: String
    ): Task<Void> {
        return usersRef.child(uid).child("username").setValue(newUsername)
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