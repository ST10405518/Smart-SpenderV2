package com.example.SmartSpender

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchDarkMode: Switch
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var spinnerLanguage: Spinner
    private lateinit var btnSaveSettings: Button

    override fun onCreate(savedInstanceState: Bundle?) {

        // Apply dark mode before setting content view
        applyDarkMode()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize views
        switchDarkMode = findViewById(R.id.switchDarkMode)
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        btnSaveSettings = findViewById(R.id.btnSaveSettings)

        // Load saved settings
        loadSettings()

        // Dark mode toggle listener
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            toggleDarkMode(isChecked)
        }

        // Save settings button
        btnSaveSettings.setOnClickListener {
            saveSettings()
        }
    }

    private fun applyDarkMode() {
        val sharedPreferences = getSharedPreferences("SmartSpenderPrefs", Context.MODE_PRIVATE)
        val nightMode = sharedPreferences.getBoolean("nightMode", false)
        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun toggleDarkMode(enable: Boolean) {
        val sharedPreferences = getSharedPreferences("SmartSpenderPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        if (enable) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            Toast.makeText(this, "Dark Mode Enabled", Toast.LENGTH_SHORT).show()
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Toast.makeText(this, "Light Mode Enabled", Toast.LENGTH_SHORT).show()
        }

        editor.putBoolean("nightMode", enable)
        editor.apply()

        // Restart activity to apply theme
        recreate()
    }

    private fun loadSettings() {
        val sharedPref = getSharedPreferences("SmartSpenderPrefs", Context.MODE_PRIVATE)

        // Dark Mode
        switchDarkMode.isChecked = sharedPref.getBoolean("nightMode", false)

        // User info
        etUsername.setText(sharedPref.getString("username", ""))
        etEmail.setText(sharedPref.getString("email", ""))
        etPhone.setText(sharedPref.getString("phone", ""))

        // Language spinner
        val language = sharedPref.getString("language", "English")
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.languages,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = adapter

        val position = adapter.getPosition(language).takeIf { it >= 0 } ?: 0
        spinnerLanguage.setSelection(position)
    }

    private fun saveSettings() {
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val language = spinnerLanguage.selectedItem.toString()

        // Validate inputs
        if (username.isEmpty()) {
            etUsername.error = "Username cannot be empty"
            etUsername.requestFocus()
            return
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Enter a valid email"
            etEmail.requestFocus()
            return
        }

        // Save to SharedPreferences
        val sharedPref = getSharedPreferences("SmartSpenderPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("username", username)
            putString("email", email)
            putString("phone", phone)
            putString("language", language)
            apply()
        }

        Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show()
        finish() // close activity so ProfileActivity refreshes on resume
    }
}
