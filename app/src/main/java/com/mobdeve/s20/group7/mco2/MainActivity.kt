package com.mobdeve.s20.group7.mco2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.mobdeve.s20.group7.mco2.session.UserSessionManager
import com.mobdeve.s20.group7.mco2.viewmodel.UserViewModel

class MainActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var userSessionManager: UserSessionManager

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupBaseComponents()

        try {
            initializeComponents()
            checkAuthenticationAndSession()
            setupNavigation()
        } catch (e: Exception) {
            Log.e(TAG, "Error during initialization: ${e.message}")
            navigateToLogin()
        }
    }

    private fun initializeComponents() {
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Get UserSessionManager from Application class
        userSessionManager = (application as QuickFlipApp).userSessionManager
    }

    private fun checkAuthenticationAndSession() {
        // Check Firebase authentication
        if (auth.currentUser == null) {
            Log.d(TAG, "No Firebase user found")
            navigateToLogin()
            return
        }

        // Check session status
        if (!userSessionManager.isLoggedIn()) {
            Log.d(TAG, "No active session found")
            navigateToLogin()
            return
        }

        // Load user data into ViewModel
        userSessionManager.getCurrentUser()?.let { user ->
            Log.d(TAG, "Loading user data into ViewModel: ${user.username}")
            userViewModel.setUser(user)
        } ?: run {
            Log.e(TAG, "Session exists but no user data found")
            navigateToLogin()
        }
    }

    private fun setupNavigation() {
        // Set up navigation controller
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Set up bottom navigation
        findViewById<BottomNavigationView>(R.id.bottom_navigation).apply {
            setupWithNavController(navController)
        }
    }

    private fun navigateToLogin() {
        Log.d(TAG, "Navigating to login screen")
        Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(this)
        }
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Verify session is still valid when returning to the app
        if (!userSessionManager.isLoggedIn() || auth.currentUser == null) {
            navigateToLogin()
        }
    }
}