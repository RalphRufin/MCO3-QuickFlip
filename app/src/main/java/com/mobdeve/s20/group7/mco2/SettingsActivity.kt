package com.mobdeve.s20.group7.mco2

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupToolbar()
        setupSharedPreferences()
        setupClickListeners()
        restoreNotificationState()
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Settings"
        }
        val drawerButton = findViewById<ImageButton>(R.id.drawerButton)
        drawerButton.setOnClickListener {
            finish()
        }
    }

    private fun setupSharedPreferences() {
        sharedPreferences = getSharedPreferences("SettingsPreferences", Context.MODE_PRIVATE)
    }

    private fun setupClickListeners() {
        findViewById<View>(R.id.languageSection).setOnClickListener {
            showLanguageDialog()
        }

        findViewById<View>(R.id.themeSection).setOnClickListener {
            showThemeDialog()
        }

        findViewById<View>(R.id.gesturesSection).setOnClickListener {
            showGesturesDialog()
        }

        

        val notificationSwitch = findViewById<Switch>(R.id.notificationsSwitch)
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            handleNotificationToggle(isChecked)
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Spanish", "French", "German")
        AlertDialog.Builder(this)
            .setTitle("Select Language")
            .setItems(languages) { _, which ->
                findViewById<TextView>(R.id.languageValue).text = languages[which]
                Toast.makeText(this, "Language changed to ${languages[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showThemeDialog() {
        val themes = arrayOf("Light", "Dark", "System default")
        AlertDialog.Builder(this)
            .setTitle("Select Theme")
            .setItems(themes) { _, which ->
                findViewById<TextView>(R.id.themeValue).text = themes[which]
                Toast.makeText(this, "Theme changed to ${themes[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showGesturesDialog() {
        val gestures = arrayOf(
            "Tap to flip, swipe to rate",
            "Double tap to flip, swipe to rate",
            "Tap to flip, buttons to rate"
        )
        AlertDialog.Builder(this)
            .setTitle("Gesture Controls")
            .setItems(gestures) { _, which ->
                Toast.makeText(this, "Gesture controls updated", Toast.LENGTH_SHORT).show()
            }
            .show()
    }



    private fun handleNotificationToggle(isChecked: Boolean) {
        if (isChecked) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestNotificationPermission()
                } else {
                    enableNotifications()
                }
            } else {
                enableNotifications()
            }
        } else {
            disableNotifications()
        }
    }

    private fun requestNotificationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
            AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("Notification permission is required to send notifications. Please enable it in Settings.")
                .setPositiveButton("Go to Settings") { _, _ ->
                    openAppNotificationSettings()
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATION_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableNotifications()
            } else {
                Toast.makeText(this, "Permission denied. Notifications will be disabled.", Toast.LENGTH_SHORT).show()
                findViewById<Switch>(R.id.notificationsSwitch).isChecked = false
            }
        }
    }

    private fun enableNotifications() {
        saveNotificationState(true)
        Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
    }

    private fun disableNotifications() {
        saveNotificationState(false)
        Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show()
        openAppNotificationSettings()
    }

    private fun openAppNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
        startActivity(intent)
    }

    private fun saveNotificationState(isChecked: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean("notifications_enabled", isChecked)
            apply()
        }
    }

    private fun restoreNotificationState() {
        val isEnabled = sharedPreferences.getBoolean("notifications_enabled", false)
        findViewById<Switch>(R.id.notificationsSwitch).isChecked = isEnabled
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1
    }
}
