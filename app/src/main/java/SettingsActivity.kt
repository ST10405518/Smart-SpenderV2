package com.example.SmartSpender

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchDarkMode: Switch
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var spinnerLanguage: Spinner
    private lateinit var btnSaveSettings: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved language and theme first
        applySavedLocale()
        applySavedLocale()

        applyDarkMode()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize UI components
        switchDarkMode = findViewById(R.id.switchDarkMode)
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        btnSaveSettings = findViewById(R.id.btnSaveSettings)

        // Load preferences
        loadSettings()

        // Dark Mode toggle listener
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            toggleDarkMode(isChecked)
        }

        // Save button listener
        btnSaveSettings.setOnClickListener {
            saveSettings()
        }
    }

    // ---------------------------
// DARK MODE
// ---------------------------
    private fun applyDarkMode() {
        val sharedPref = getSharedPreferences("SmartSpenderPrefs", Context.MODE_PRIVATE)
        val nightMode = sharedPref.getBoolean("nightMode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (nightMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun toggleDarkMode(enable: Boolean) {
        val sharedPref = getSharedPreferences("SmartSpenderPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("nightMode", enable)
            apply()
        }

        AppCompatDelegate.setDefaultNightMode(
            if (enable) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        Toast.makeText(
            this,
            if (enable) getString(R.string.dark_mode_enabled) else getString(R.string.light_mode_enabled),
            Toast.LENGTH_SHORT
        ).show()

        recreate() // Refresh UI
    }

    // ---------------------------
// LOAD & SAVE SETTINGS
// ---------------------------
    private fun loadSettings() {
        val sharedPref = getSharedPreferences("SmartSpenderPrefs", Context.MODE_PRIVATE)

        // Dark Mode
        switchDarkMode.isChecked = sharedPref.getBoolean("nightMode", false)

        // User details
        etUsername.setText(sharedPref.getString("username", ""))
        etEmail.setText(sharedPref.getString("email", ""))
        etPhone.setText(sharedPref.getString("phone", ""))

        // Language spinner
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.languages,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = adapter

        val savedLang = sharedPref.getString("language", "English")
        val position = adapter.getPosition(savedLang).takeIf { it >= 0 } ?: 0
        spinnerLanguage.setSelection(position)
    }

    private fun saveSettings() {
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val language = spinnerLanguage.selectedItem.toString()

        // Validation
        if (username.isEmpty()) {
            etUsername.error = getString(R.string.error_username_empty)
            etUsername.requestFocus()
            return
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = getString(R.string.error_email_invalid)
            etEmail.requestFocus()
            return
        }

        // Save settings
        val sharedPref = getSharedPreferences("SmartSpenderPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("username", username)
            putString("email", email)
            putString("phone", phone)
            putString("language", language)
            apply()
        }

        // Apply language immediately
        val langCode = when (language) {
            "Zulu" -> "zu"
            "Afrikaans" -> "af"
            "Xhosa" -> "xh"
            "Tshivenda" -> "ve"
            else -> "en"
        }
        applyLocale(langCode)

        Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
        recreate()
    }

    // ---------------------------
// LANGUAGE HANDLING
// ---------------------------
    private fun applyLocale(langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        // Update app context
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)

        // Save code
        val sharedPref = getSharedPreferences("SmartSpenderPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("language_code", langCode)
            apply()
        }
    }

    private fun applySavedLocale() {
        val sharedPref = getSharedPreferences("SmartSpenderPrefs", Context.MODE_PRIVATE)
        val lang = sharedPref.getString("language_code", "en") ?: "en"
        applyLocale(lang)
    }

}
