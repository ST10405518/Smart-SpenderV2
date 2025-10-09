package com.example.SmartSpender

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.SmartSpender.database.DatabaseHelper

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var ivBack: ImageView
    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        // Apply dark mode before setting content view
        applyDarkMode()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize views
        tvName = findViewById(R.id.tvProfileName)
        tvEmail = findViewById(R.id.tvProfileEmail)
        tvPhone = findViewById(R.id.tvProfileNumber)
        ivBack = findViewById(R.id.ivBack)
        dbHelper = DatabaseHelper(this)

        // Back button
        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Security layout click
        val layoutSecurity = findViewById<LinearLayout>(R.id.layoutSecurity)
        layoutSecurity.setOnClickListener {
            val intent = Intent(this, SecurityActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Load user info every time the activity resumes to reflect changes
        loadUserInfo()
    }

    private fun loadUserInfo() {
        val sharedPref = getSharedPreferences("SmartSpenderPrefs", Context.MODE_PRIVATE)

        // Get data from SharedPreferences (keys must match SettingsActivity)
        val username = sharedPref.getString("username", "Unknown User")
        val email = sharedPref.getString("email", "Not available")
        val phone = sharedPref.getString("phone", "Not available")

        // Set TextViews
        tvName.text = username
        tvEmail.text = email
        tvPhone.text = phone
    }

    private fun applyDarkMode() {
        val sharedPref = getSharedPreferences("SmartSpenderPrefs", Context.MODE_PRIVATE)
        val nightMode = sharedPref.getBoolean("nightMode", false)
        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
