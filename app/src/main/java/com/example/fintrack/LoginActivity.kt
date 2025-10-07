package com.example.SmartSpender

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.SmartSpender.database.DatabaseHelper
import com.example.SmartSpender.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnSignUp: Button
    private lateinit var btnGoogleSignIn: Button
    private lateinit var dbHelper: DatabaseHelper

    // Google Sign-in variables
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private companion object {
        private const val RC_SIGN_IN = 123
        private const val TAG = "GoogleSignIn"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnSignUp = findViewById(R.id.btnSignUp)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)

        // Initialize database helper
        dbHelper = DatabaseHelper(this)

        // Initialize Firebase Auth and Google Sign-in
        initGoogleSignIn()

        // Set click listener for Login button
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (validateInputs(email, password)) {
                val user = dbHelper.getUserByEmail(email)
                if (user != null && user.password == password) {
                    saveUserSession(user)
                    navigateToMainActivity()
                } else {
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set click listener for Google Sign-in button
        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        // Set click listener for Sign Up button
        btnSignUp.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun initGoogleSignIn() {
        // Configure Google Sign In - USING YOUR NEW CLIENT ID
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("46478644171-9q0eiafvkfgf94ffikdvmlvhb1n3kuem.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()
        Log.d(TAG, "Google Sign-in initialized with new project")
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "Google Sign-in successful: ${account.email}")
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed
                Log.e(TAG, "Google sign in failed: ${e.statusCode} - ${e.message}")
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, get Firebase user
                    val firebaseUser = firebaseAuth.currentUser
                    Log.d(TAG, "Firebase authentication successful: ${firebaseUser?.email}")

                    if (firebaseUser != null) {
                        handleGoogleUser(firebaseUser.email ?: "", firebaseUser.displayName ?: "Google User")
                    }
                } else {
                    // Sign in failed
                    Log.e(TAG, "Firebase authentication failed: ${task.exception?.message}")
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun handleGoogleUser(email: String, name: String) {
        // Check if user exists in local database
        val existingUser = dbHelper.getUserByEmail(email)

        if (existingUser == null) {
            // Create new user in local database
            val newUser = User(
                id = 0,
                name = name,
                email = email,
                mobile = "",
                dob = "",
                password = "google_oauth" // Special password for Google users
            )

            val userId = dbHelper.addUser(newUser)

            if (userId > 0) {
                saveUserSession(newUser.copy(id = userId.toInt()))
                Toast.makeText(this, "Google sign in successful!", Toast.LENGTH_SHORT).show()
                navigateToMainActivity()
            } else {
                Toast.makeText(this, "Failed to create user account", Toast.LENGTH_SHORT).show()
            }
        } else {
            // User exists, log them in
            saveUserSession(existingUser)
            Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
            navigateToMainActivity()
        }
    }

    private fun saveUserSession(user: User) {
        val sharedPref = getSharedPreferences("SmartSpenderPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("userId", user.id)
            putString("userEmail", user.email)
            putString("userName", user.name)
            apply()
        }
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Enter a valid email"
            etEmail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            etPassword.requestFocus()
            return false
        }

        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            etPassword.requestFocus()
            return false
        }

        return true
    }

    override fun onStart() {
        super.onStart()
        // Check if user is already signed in
        val sharedPref = getSharedPreferences("SmartSpenderPrefs", MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", -1)
        if (userId != -1) {
            navigateToMainActivity()
        }
    }
}