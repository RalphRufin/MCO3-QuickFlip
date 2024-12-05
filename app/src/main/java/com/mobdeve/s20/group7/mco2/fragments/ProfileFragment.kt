package com.mobdeve.s20.group7.mco2.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mobdeve.s20.group7.mco2.LoginActivity
import com.mobdeve.s20.group7.mco2.R
import com.mobdeve.s20.group7.mco2.viewmodel.UserViewModel
import com.mobdeve.s20.group7.mco2.session.UserSessionManager
import com.mobdeve.s20.group7.mco2.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {
    // Change viewModels() to activityViewModels() to share ViewModel with activity
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var userSessionManager: UserSessionManager
    private lateinit var auth: FirebaseAuth

    private lateinit var profilePicture: ImageView
    private lateinit var usernameText: TextView
    private lateinit var emailText: TextView
    private lateinit var logoutButton: MaterialButton
    private lateinit var deleteButton: MaterialButton


    companion object {
        private const val TAG = "ProfileFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize UserSessionManager
        userSessionManager = UserSessionManager(requireContext())

        // Initialize views
        try {
            profilePicture = view.findViewById(R.id.profilePicture)
            usernameText = view.findViewById(R.id.usernameText)
            emailText = view.findViewById(R.id.emailText)
            logoutButton = view.findViewById(R.id.logoutButton)
            deleteButton = view.findViewById(R.id.deleteButton)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}")
            return
        }

        // Set up logout button
        logoutButton.setOnClickListener {
            logout()
        }
        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // Load initial data from session
        try {
            val sessionUser = userSessionManager.getCurrentUser()
            if (sessionUser != null) {
                updateUI(sessionUser)
                // Update ViewModel with session data
                userViewModel.setUser(sessionUser)
            } else {
                Log.e(TAG, "No user session found")
                navigateToLogin()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user session: ${e.message}")
            navigateToLogin()
        }

        // Observe user data changes
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                try {
                    updateUI(it)
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating UI: ${e.message}")
                }
            }
        }
    }

    private fun updateUI(user: User) {
        try {
            // Set username and email
            usernameText.text = user.username.ifEmpty { "No username" }
            emailText.text = user.email.ifEmpty { "No email" }

            // Load profile picture using Glide
            if (user.profilePicUrl.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(user.profilePicUrl)
                    .placeholder(R.drawable.default_profile_picture)
                    .error(R.drawable.default_profile_picture)
                    .circleCrop()
                    .into(profilePicture)
            } else {
                profilePicture.setImageResource(R.drawable.default_profile_picture)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateUI: ${e.message}")
        }
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Profile")
            .setMessage("Are you sure you want to delete your profile? This action cannot be undone.")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Delete") { _, _ ->
                deleteProfile()
            }
            .show()
    }

    private fun deleteProfile() {
        try {
            // Delete user data from Firestore
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userId = currentUser.uid
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("users").document(userId)
                    .delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "User data deleted successfully")

                        // Delete Firebase Authentication account
                        currentUser.delete()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d(TAG, "User account deleted")
                                    userSessionManager.clearSession()
                                    userViewModel.clearUser()
                                    navigateToLogin()
                                } else {
                                    Log.e(TAG, "Failed to delete user account: ${task.exception?.message}")
                                }
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error deleting user data: ${e.message}")
                    }
            } else {
                Log.e(TAG, "No authenticated user found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in deleteProfile: ${e.message}")
        }
    }


    private fun logout() {
        try {
            // Sign out from Firebase
            auth.signOut()
            // Clear local session
            userSessionManager.clearSession()
            // Clear ViewModel
            userViewModel.clearUser()
            // Navigate to login activity
            navigateToLogin()
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout: ${e.message}")
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(requireActivity(), LoginActivity::class.java))
        requireActivity().finish()
    }
}