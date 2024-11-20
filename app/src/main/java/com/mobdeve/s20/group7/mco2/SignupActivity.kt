package com.mobdeve.s20.group7.mco2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mobdeve.s20.group7.mco2.models.DeckItem
import com.mobdeve.s20.group7.mco2.utils.FirebaseHelper

class SignupActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseHelper: FirebaseHelper

    // UI components
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signupButton: Button

    companion object {
        private const val TAG = "SignupActivity"
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = Firebase.auth
        firebaseHelper = FirebaseHelper.getInstance()

        // Initialize UI components
        initializeViews()
        setupClickListeners()
        setupSocialLogin()
    }

    private fun initializeViews() {
        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        signupButton = findViewById(R.id.signupButton)
    }

    private fun setupClickListeners() {
        signupButton.setOnClickListener {
            handleEmailPasswordSignup()
        }

        findViewById<TextView>(R.id.alreadyHaveAccountText).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupSocialLogin() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<ImageView>(R.id.googleSignup).setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                signInWithGoogle()
            }
        }
    }

    private fun handleEmailPasswordSignup() {
        val username = usernameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        // Validation
        when {
            username.isEmpty() -> {
                usernameEditText.error = "Username required"
                return
            }
            email.isEmpty() -> {
                emailEditText.error = "Email required"
                return
            }
            password.isEmpty() -> {
                passwordEditText.error = "Password required"
                return
            }
            password != confirmPassword -> {
                confirmPasswordEditText.error = "Passwords don't match"
                return
            }
            password.length < 6 -> {
                passwordEditText.error = "Password should be at least 6 characters"
                return
            }
        }

        // Create user with email and password
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    user?.let {
                        saveUserToDatabase(
                            uid = it.uid,
                            username = username,
                            email = email,
                            authMethod = "email"
                        )
                    }
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Signup failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToDatabase(
        uid: String,
        username: String,
        email: String,
        authMethod: String,
        profilePicUrl: String = ""
    ) {
        val userData = hashMapOf(
            "username" to username,
            "email" to email,
            "authMethod" to authMethod,
            "profilePicUrl" to profilePicUrl,
            "points" to 0,  // Initialize points to 0
            "deckItems" to ArrayList<DeckItem>()
        )

        firebaseHelper.db.collection("users").document(uid)
            .set(userData)
            .addOnSuccessListener {
                // Initialize missions document
                firebaseHelper.db.collection("users").document(uid)
                    .collection("missions")
                    .document("daily")
                    .set(mapOf(
                        "login" to false,
                        "test1Deck" to false,
                        "test2Decks" to false,
                        "lastResetTime" to System.currentTimeMillis()
                    ))
                    .addOnSuccessListener {
                        Log.d(TAG, "User data and missions saved successfully")
                        Toast.makeText(baseContext, "Signup successful!", Toast.LENGTH_SHORT).show()
                        navigateToLogin()
                    }
            }.addOnFailureListener { e ->
                Log.w(TAG, "Error saving user data", e)
                Toast.makeText(baseContext, "Error saving user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleSocialSignInUser(user: FirebaseUser, authMethod: String) {
        val username = user.displayName ?: "User${System.currentTimeMillis()}"
        val email = user.email ?: ""
        val profilePicUrl = user.photoUrl?.toString() ?: ""

        // Save user to database
        saveUserToDatabase(
            uid = user.uid,
            username = username,
            email = email,
            authMethod = authMethod,
            profilePicUrl = profilePicUrl
        )
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let { handleSocialSignInUser(it, "google") }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
