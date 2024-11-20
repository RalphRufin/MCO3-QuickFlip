package com.mobdeve.s20.group7.mco2

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.mobdeve.s20.group7.mco2.models.DeckItem
import com.mobdeve.s20.group7.mco2.models.User
import com.mobdeve.s20.group7.mco2.session.UserSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.view.animation.Animation
import android.view.animation.AnimationUtils

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var db: FirebaseFirestore
    private lateinit var sessionManager: UserSessionManager
    private lateinit var loadingView: View // Add a loading view to your layout
    private lateinit var loadingImage: ImageView

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize components
        initializeComponents()
        setupClickListeners()
        setupGoogleSignIn()

        // Removed the session check, so the login page is always shown now
    }

    private fun initializeComponents() {
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        sessionManager = UserSessionManager(this)
        loadingView = findViewById(R.id.loadingView)
        loadingImage = findViewById(R.id.loadingImage)

        // Start the rotation animation
        val rotateAnim: Animation = AnimationUtils.loadAnimation(this, R.anim.spin)
        loadingImage.startAnimation(rotateAnim)
    }

    private fun setupClickListeners() {
        findViewById<TextView>(R.id.signUpText).setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        findViewById<ImageView>(R.id.googleLogin).setOnClickListener {
            if (!isNetworkAvailable()) {
                showError("No internet connection")
                return@setOnClickListener
            }
            showLoading(true)
            lifecycleScope.launch {
                googleSignInClient.signOut().await()
                signInWithGoogle()
            }
        }

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            val email = findViewById<EditText>(R.id.emailEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()
            if (validateInput(email, password)) {
                loginWithEmail(email, password)
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields")
            return false
        }
        if (!isNetworkAvailable()) {
            showError("No internet connection")
            return false
        }
        return true
    }

    private fun loginWithEmail(email: String, password: String) {
        showLoading(true)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let { user ->
                    fetchUserDataAndSaveSession(user, "email")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Invalid login credentials")
                    showLoading(false)
                }
            }
        }
    }

    private suspend fun fetchUserDataAndSaveSession(firebaseUser: FirebaseUser, authMethod: String) {
        try {
            val document = db.collection("users").document(firebaseUser.uid).get().await()
            if (document != null && document.exists()) {
                val user = User(
                    username = document.getString("username") ?: "",
                    email = firebaseUser.email ?: "",
                    profilePicUrl = firebaseUser.photoUrl?.toString() ?: "",
                    authMethod = authMethod,
                    deckItems = (document.get("deckItems") as? List<DeckItem>)?.let { ArrayList(it) }
                        ?: ArrayList(),
                    points = document.getLong("points")?.toInt() ?: 0  // Get points from Firestore
                )

                // Update login mission status
                db.collection("users")
                    .document(firebaseUser.uid)
                    .collection("missions")
                    .document("daily")
                    .update(mapOf(
                        "login" to true
                    )).await()

                withContext(Dispatchers.Main) {
                    sessionManager.saveUserSession(user)
                    navigateToMain()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                showError("Error fetching user data: ${e.message}")
                showLoading(false)
            }
        }
    }

    private suspend fun firebaseAuthWithGoogle(idToken: String) {
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            result.user?.let { user ->
                checkUserInFirestore(user)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                showError("Authentication failed: ${e.message}")
                showLoading(false)
            }
        }
    }

    private suspend fun checkUserInFirestore(user: FirebaseUser) {
        try {
            val document = db.collection("users").document(user.uid).get().await()
            if (document != null && document.exists()) {
                fetchUserDataAndSaveSession(user, "google")
            } else {
                withContext(Dispatchers.Main) {
                    auth.signOut()
                    googleSignInClient.signOut().await()
                    showError("No account found. Please sign up first.")
                    showLoading(false)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                auth.signOut()
                googleSignInClient.signOut()
                showError("Database error: ${e.message}")
                showLoading(false)
            }
        }
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            lifecycleScope.launch {
                try {
                    val account = GoogleSignIn.getSignedInAccountFromIntent(data).await()
                    account.idToken?.let { firebaseAuthWithGoogle(it) }
                } catch (e: ApiException) {
                    showError("Google sign in failed")
                    showLoading(false)
                }
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    private fun showLoading(show: Boolean) {
        loadingView.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
