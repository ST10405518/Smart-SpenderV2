package com.example.SmartSpender

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.SmartSpender.database.DatabaseHelper
import com.example.SmartSpender.models.User
import java.text.SimpleDateFormat
import java.util.*

class SignupActivity : AppCompatActivity() {
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etMobile: EditText
    private lateinit var etDob: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var dbHelper: DatabaseHelper
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize views
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etMobile = findViewById(R.id.etMobile)
        etDob = findViewById(R.id.etDob)
        etPassword = findViewById(R.id.etPassword)
        btnSignUp = findViewById(R.id.btnSignUp)

        // Initialize database helper
        dbHelper = DatabaseHelper(this)

        // Set up date picker for date of birth
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        etDob.setOnClickListener {
            DatePickerDialog(
                this, dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Set click listener for Sign Up button
        btnSignUp.setOnClickListener {
            val name = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val mobile = etMobile.text.toString().trim()
            val dob = etDob.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (validateInputs(name, email, mobile, dob, password)) {
                // Check if user already exists
                if (dbHelper.getUserByEmail(email) != null) {
                    Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Create new user
                val user = User(0, name, email, mobile, dob, password)
                val userId = dbHelper.addUser(user)

                if (userId > 0) {
                    // Save user session
                    val sharedPref = getSharedPreferences("SmartSpenderPrefs", MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putInt("userId", userId.toInt())
                        putString("userEmail", email)
                        putString("userName", name)
                        apply()
                    }

                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateDateInView() {
        val format = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        etDob.setText(sdf.format(calendar.time))
    }

    private fun validateInputs(name: String, email: String, mobile: String, dob: String, password: String): Boolean {
        if (name.isEmpty()) {
            etFullName.error = "Name is required"
            etFullName.requestFocus()
            return false
        }

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

        if (mobile.isEmpty()) {
            etMobile.error = "Mobile number is required"
            etMobile.requestFocus()
            return false
        }

        if (mobile.length < 10) {
            etMobile.error = "Enter a valid mobile number"
            etMobile.requestFocus()
            return false
        }

        if (dob.isEmpty()) {
            etDob.error = "Date of birth is required"
            etDob.requestFocus()
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
}
