package com.example.SmartSpender

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.SmartSpender.database.DatabaseHelper
import com.example.SmartSpender.models.Wallet
import com.google.android.material.bottomnavigation.BottomNavigationView

class AddWalletActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var etNameOnCard: EditText
    private lateinit var etCardNumber: EditText
    private lateinit var etCVC: EditText
    private lateinit var etExpirationDate: EditText
    private lateinit var etZip: EditText
    private lateinit var btnDiscard: Button
    private lateinit var btnAdd: Button
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_wallet)

        // Get user ID from shared preferences
        val sharedPref = getSharedPreferences("SmartSpenderPrefs", MODE_PRIVATE)
        userId = sharedPref.getInt("userId", 0)

        if (userId == 0) {
            // User not logged in, redirect to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialize views
        etNameOnCard = findViewById(R.id.etNameOnCard)
        etCardNumber = findViewById(R.id.etCardNumber)
        etCVC = findViewById(R.id.etCVC)
        etExpirationDate = findViewById(R.id.etExpirationDate)
        etZip = findViewById(R.id.etZip)
        btnDiscard = findViewById(R.id.btnDiscard)
        btnAdd = findViewById(R.id.btnAdd)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Initialize database helper
        dbHelper = DatabaseHelper(this)

        // Set up bottom navigation
        bottomNavigation.setOnNavigationItemSelectedListener(this)
        bottomNavigation.menu.findItem(R.id.navigation_wallet).isChecked = true

        // Set up Discard button
        btnDiscard.setOnClickListener {
            finish()
        }

        // Set up Add button
        btnAdd.setOnClickListener {
            saveWallet()
        }
    }

    private fun saveWallet() {
        try {
            val nameOnCard = etNameOnCard.text.toString().trim()
            val cardNumber = etCardNumber.text.toString().trim()
            val cvc = etCVC.text.toString().trim()
            val expirationDate = etExpirationDate.text.toString().trim()
            val zip = etZip.text.toString().trim()

            if (nameOnCard.isEmpty()) {
                etNameOnCard.error = "Name on card is required"
                etNameOnCard.requestFocus()
                return
            }

            if (cardNumber.isEmpty()) {
                etCardNumber.error = "Card number is required"
                etCardNumber.requestFocus()
                return
            }

            if (cvc.isEmpty()) {
                etCVC.error = "CVC is required"
                etCVC.requestFocus()
                return
            }

            if (expirationDate.isEmpty()) {
                etExpirationDate.error = "Expiration date is required"
                etExpirationDate.requestFocus()
                return
            }

            if (zip.isEmpty()) {
                etZip.error = "Zip code is required"
                etZip.requestFocus()
                return
            }

            // Create wallet object
            val wallet = Wallet(
                0,
                userId,
                nameOnCard,
                cardNumber,
                cvc,
                expirationDate,
                zip,
                0.0// Default balance
            )

            // Save wallet to database
            val walletId = dbHelper.addWallet(wallet)

            if (walletId > 0) {
                Toast.makeText(this, "Wallet added successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Failed to add wallet", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_home -> {
                startActivity(Intent(this, MainActivity::class.java))
                return true
            }
            R.id.navigation_stats -> {
                startActivity(Intent(this, StatisticsActivity::class.java))
                return true
            }
            R.id.navigation_add -> {
                startActivity(Intent(this, AddSpendingActivity::class.java))
                return true
            }
            R.id.navigation_wallet -> {
                return true
            }
            R.id.navigation_notifications -> {
                startActivity(Intent(this, NotificationActivity::class.java))
                return true
            }
        }
        return false
    }
}
