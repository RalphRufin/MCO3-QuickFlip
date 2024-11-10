package com.mobdeve.s20.group7.mco2

import android.app.Application
import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp
import com.mobdeve.s20.group7.mco2.session.UserSessionManager

class QuickFlipApp : MultiDexApplication() {

    lateinit var userSessionManager: UserSessionManager

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize the UserSessionManager
        userSessionManager = UserSessionManager(this)
    }

    companion object {
        lateinit var instance: QuickFlipApp
            private set
    }

    init {
        instance = this
    }
}
