package com.mobdeve.s20.group7.mco2

import android.app.AlertDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupToolbar()
        setupClickListeners()
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

    private fun setupClickListeners() {
        findViewById<View>(R.id.languageSection).setOnClickListener {
            showLanguageDialog()
        }

        findViewById<View>(R.id.syncSection).setOnClickListener {
            showSyncDialog()
        }


        findViewById<View>(R.id.themeSection).setOnClickListener {
            showThemeDialog()
        }

        findViewById<View>(R.id.gesturesSection).setOnClickListener {
            showGesturesDialog()
        }

        findViewById<View>(R.id.backupSection).setOnClickListener {
            showBackupDialog()
        }

        findViewById<Switch>(R.id.notificationsSwitch).setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, "Notifications ${if (isChecked) "enabled" else "disabled"}",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Spanish", "French", "German")
        AlertDialog.Builder(this)
            .setTitle("Select Language")
            .setItems(languages) { dialog, which ->
                findViewById<TextView>(R.id.languageValue).text = languages[which]
                Toast.makeText(this, "Language changed to ${languages[which]}",
                    Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showSyncDialog() {
        val syncOptions = arrayOf("Every hour", "Every 6 hours", "Every 12 hours", "Daily")
        AlertDialog.Builder(this)
            .setTitle("Sync Frequency")
            .setItems(syncOptions) { dialog, which ->
                findViewById<TextView>(R.id.syncValue).text = syncOptions[which]
                Toast.makeText(this, "Sync frequency updated", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showThemeDialog() {
        val themes = arrayOf("Light", "Dark", "System default")
        AlertDialog.Builder(this)
            .setTitle("Select Theme")
            .setItems(themes) { dialog, which ->
                findViewById<TextView>(R.id.themeValue).text = themes[which]
                Toast.makeText(this, "Theme changed to ${themes[which]}",
                    Toast.LENGTH_SHORT).show()
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
            .setItems(gestures) { dialog, which ->
                Toast.makeText(this, "Gesture controls updated", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showBackupDialog() {
        val backupOptions = arrayOf("Daily", "Weekly", "Monthly", "Never")
        AlertDialog.Builder(this)
            .setTitle("Auto Backup Frequency")
            .setItems(backupOptions) { dialog, which ->
                findViewById<TextView>(R.id.backupValue).text = backupOptions[which]
                Toast.makeText(this, "Backup frequency updated", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}