package com.mobdeve.s20.group7.mco2

import android.app.Application
import com.facebook.FacebookSdk

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(applicationContext)
    }
}