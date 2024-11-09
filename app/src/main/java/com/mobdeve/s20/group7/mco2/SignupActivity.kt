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
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mobdeve.s20.group7.mco2.utils.FirebaseHelper

class SignupActivity : AppCompatActivity() {
    private lateinit var callbackManager: CallbackManager
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
        // Initialize Facebook Login
        callbackManager = CallbackManager.Factory.create()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Facebook sign-out to clear last session
        findViewById<ImageView>(R.id.facebookSignup).setOnClickListener {
            LoginManager.getInstance().logOut() // Log out from Facebook
            val permissions = listOf("email", "public_profile")
            LoginManager.getInstance().logIn(this, permissions) // Re-initiate Facebook login
        }

        // Google sign-out to clear last session
        findViewById<ImageView>(R.id.googleSignup).setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                signInWithGoogle() // Re-initiate Google login
            }
        }

        // Facebook callback registration
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    Log.d(TAG, "facebook:onCancel")
                    Toast.makeText(baseContext, "Facebook login cancelled",
                        Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: FacebookException) {
                    Log.d(TAG, "facebook:onError", error)
                    Toast.makeText(baseContext, "Facebook login failed",
                        Toast.LENGTH_SHORT).show()
                }
            })
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
                    navigateToMain()
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    // Save additional user data to Firebase database and navigate to MainActivity
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
        firebaseHelper.createNewUser(
            uid = uid,
            username = username,
            email = email,
            authMethod = authMethod,
            profilePicUrl = profilePicUrl
        ).addOnSuccessListener {
            Log.d(TAG, "User data saved successfully")
            Toast.makeText(baseContext, "Signup successful!", Toast.LENGTH_SHORT).show()
            navigateToMain() // Navigate to MainActivity upon successful data save
        }.addOnFailureListener { e ->
            Log.w(TAG, "Error saving user data", e)
            Toast.makeText(baseContext, "Error saving user data", Toast.LENGTH_SHORT).show()
        }
    }


    private fun handleSocialSignInUser(user: FirebaseUser, authMethod: String) {
        val username = user.displayName ?: "User${System.currentTimeMillis()}"
        val email = user.email ?: ""
        val profilePicUrl = user.photoUrl?.toString() ?: ""

        saveUserToDatabase(
            uid = user.uid,
            username = username,
            email = email,
            authMethod = authMethod,
            profilePicUrl = profilePicUrl
        )
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data)

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

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToMain()
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    user?.let { handleSocialSignInUser(it, "google") }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToMain()
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    user?.let { handleSocialSignInUser(it, "facebook") }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
