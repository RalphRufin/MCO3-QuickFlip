package com.mobdeve.s20.group7.mco2

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.security.MessageDigest


class InitloadActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initload)

        try {
            val info = packageManager.getPackageInfo(
                "com.mobdeve.s20.group7.mco2",  // Replace with your app's package name
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: Exception) {
            // Handle error
        }


        val getStartedButton = findViewById<Button>(R.id.getStartedButton)

        val logInLinkTextView = findViewById<TextView>(R.id.logInLinkTextView)


        getStartedButton.setOnClickListener {
        val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }


        logInLinkTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
