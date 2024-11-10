package com.mobdeve.s20.group7.mco2

import android.app.Application
import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp

class QuickFlipApp : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}