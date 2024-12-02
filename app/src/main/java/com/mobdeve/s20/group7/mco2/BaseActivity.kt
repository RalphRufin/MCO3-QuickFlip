package com.mobdeve.s20.group7.mco2

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView

open class BaseActivity : AppCompatActivity() {

    protected lateinit var drawerLayout: DrawerLayout
    protected lateinit var navigationView: NavigationView
    protected lateinit var toolbar: androidx.appcompat.widget.Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    protected fun setupBaseComponents() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = null

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_menu)

        val logoImageView: ImageButton = findViewById(R.id.logoImageView)
        val drawerButton: ImageButton = findViewById(R.id.drawerButton)

        logoImageView.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        drawerButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        setupNavigationView()
    }

    private fun setupNavigationView() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                R.id.nav_help -> {
                    showManualDialog()
                    true
                }
                R.id.nav_support -> {
                    showRatingDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun showManualDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialogue_manual)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.manualRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val manualSections = listOf(
            ManualSection(
                "Getting Started",
                "Welcome to QuickFlip! This flashcard app helps you memorize anything efficiently using spaced repetition. Create decks for different subjects and add cards to start learning."
            ),
            ManualSection(
                "Creating Decks",
                "Tap the '+' button on the main screen to create a new deck. Give it a name and optional description. You can organize your cards by subject, topic, or any way you prefer."
            ),
            ManualSection(
                "Adding Cards",
                "Open a deck and tap '+' to add new cards. Each card has a front (question) and back (answer). You can include text, basic formatting, and hints in your cards."
            ),
            ManualSection(
                "Study Session",
                "During review, cards are shown based on spaced repetition. After viewing the answer, rate your recall from 1-4. This helps QuickFlip schedule the next review optimally."
            ),
            ManualSection(
                "Spaced Repetition",
                "QuickFlip uses an algorithm similar to Anki to schedule reviews. Better-remembered cards are shown less frequently, while challenging ones appear more often."
            ),
            ManualSection(
                "Statistics",
                "Track your progress through the Statistics page. View your daily review counts, success rates, and upcoming reviews to stay motivated and consistent."
            ),
            ManualSection(
                "Backup and Sync",
                "Regularly backup your decks using the export feature in the menu. You can import decks and restore backups as needed."
            ),
            ManualSection(
                "Tips for Success",
                "• Review cards daily for best results\n• Keep cards simple and focused\n• Use mnemonics and memory techniques\n• Customize review schedules as needed"
            )
        )

        val adapter = ManualAdapter(manualSections)
        recyclerView.adapter = adapter

        drawerLayout.closeDrawer(GravityCompat.START)

        dialog.show()
    }

    private fun showRatingDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialogue_rating)

        val stars = listOf<ImageButton>(
            dialog.findViewById(R.id.star1),
            dialog.findViewById(R.id.star2),
            dialog.findViewById(R.id.star3),
            dialog.findViewById(R.id.star4),
            dialog.findViewById(R.id.star5)
        )

        var currentRating = 0

        stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                currentRating = index + 1
                stars.forEachIndexed { i, starButton ->
                    starButton.setImageResource(
                        if (i <= index) R.drawable.star_yellow
                        else R.drawable.star_gray
                    )
                }
            }
        }

        dialog.findViewById<Button>(R.id.submitButton).setOnClickListener {
            if (currentRating > 0) {
                Toast.makeText(this, "Thank you for rating Quickflip: $currentRating stars!", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        drawerLayout.closeDrawer(GravityCompat.START)

        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.overflow_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_check -> {
                Toast.makeText(this, "Checking database...", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_restore -> {
                showRestoreDialog()
                true
            }
            R.id.action_import -> {
                showImportDialog()
                true
            }
            R.id.action_export -> {
                showExportDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showRestoreDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialogue_restore_backup, null)

        builder.setView(dialogView)
        val dialog = builder.create()

        dialogView.findViewById<TextView>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.continueButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showImportDialog() {
        val options = arrayOf("Plain text (.txt)", "Comma separated value (.csv)", "Markdown (.md)")

        AlertDialog.Builder(this)
            .setTitle("Import Format")
            .setItems(options) { dialog, which ->
                val format = when (which) {
                    0 -> "txt"
                    1 -> "csv"
                    2 -> "md"
                    else -> "txt"
                }
                Toast.makeText(this, "Importing $format...", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .show()
    }

    private fun showExportDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialogue_export, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val spinner = dialogView.findViewById<Spinner>(R.id.formatSpinner)
        val includeImagesCheckbox = dialogView.findViewById<CheckBox>(R.id.includeImagesCheckbox)

        val formats = arrayOf("Plain text (.txt)", "Comma separated value (.csv)", "Markdown (.md)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, formats)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        dialogView.findViewById<TextView>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.confirmButton).setOnClickListener {
            val selectedFormat = when (spinner.selectedItemPosition) {
                0 -> "txt"
                1 -> "csv"
                2 -> "md"
                else -> "txt"
            }
            val includeImages = includeImagesCheckbox.isChecked
            Toast.makeText(this, "Exporting as $selectedFormat (Images: $includeImages)", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}