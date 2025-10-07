package com.example.SmartSpender

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.SmartSpender.database.DatabaseHelper
import com.example.SmartSpender.models.Notification
import com.example.SmartSpender.models.Wallet
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.NumberFormat
import java.util.*

class EditWalletActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var tvBalance: TextView
    private lateinit var etNameOnCard: EditText
    private lateinit var etCardNumber: EditText
    private lateinit var etCVC: EditText
    private lateinit var etExpirationDate: EditText
    private lateinit var etZip: EditText
    private lateinit var btnDelete: Button
    private lateinit var btnEdit: Button
    private lateinit var fabAddFunds: FloatingActionButton
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = 0
    private var wallet: Wallet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_wallet)

        // Get user ID from shared preferences
        val sharedPref = getSharedPreferences("SmartSpenderPrefs", MODE_PRIVATE)
        userId = sharedPref.getInt("userId", 0)

        if (userId == 0) {
            // User not logged in, redirect to login
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialize views
        tvBalance = findViewById(R.id.tvBalance)
        etNameOnCard = findViewById(R.id.etNameOnCard)
        etCardNumber = findViewById(R.id.etCardNumber)
        etCVC = findViewById(R.id.etCVC)
        etExpirationDate = findViewById(R.id.etExpirationDate)
        etZip = findViewById(R.id.etZip)
        btnDelete = findViewById(R.id.btnDelete)
        btnEdit = findViewById(R.id.btnEdit)
        fabAddFunds = findViewById(R.id.fabAddFunds)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Initialize database helper
        dbHelper = DatabaseHelper(this)

        // Set up bottom navigation
        bottomNavigation.setOnNavigationItemSelectedListener(this)
        bottomNavigation.menu.findItem(R.id.navigation_wallet).isChecked = true

        // Load wallet data
        loadWalletData()

        // Set up Delete button
        btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // Set up Edit button
        btnEdit.setOnClickListener {
            updateWallet()
        }

        // Set up Add Funds button
        fabAddFunds.setOnClickListener {
            showAddFundsDialog()
        }
    }

    private fun loadWalletData() {
        wallet = dbHelper.getWalletByUserId(userId)
        if (wallet != null) {
            // Format balance
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            currencyFormat.currency = Currency.getInstance("ZAR")
            tvBalance.text = currencyFormat.format(wallet!!.balance).replace("ZAR", "R")

            // Set card details
            etNameOnCard.setText(wallet!!.nameOnCard)
            etCardNumber.setText(wallet!!.cardNumber)
            etCVC.setText(wallet!!.cvc)
            etExpirationDate.setText(wallet!!.expirationDate)
            etZip.setText(wallet!!.zip)
        } else {
            // No wallet found, redirect to add wallet
            startActivity(Intent(this, AddWalletActivity::class.java))
            finish()
        }
    }

    private fun updateWallet() {
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

        // Update wallet object
        wallet?.let {
            it.nameOnCard = nameOnCard
            it.cardNumber = cardNumber
            it.cvc = cvc
            it.expirationDate = expirationDate
            it.zip = zip

            // Update wallet in database
            val success = dbHelper.updateWallet(it)

            if (success) {
                Toast.makeText(this, "Wallet updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to update wallet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Wallet")
            .setMessage("Are you sure you want to delete this wallet?")
            .setPositiveButton("Delete") { _, _ ->
                if (wallet != null) {
                    val success = dbHelper.deleteWallet(wallet!!.id)
                    if (success) {
                        Toast.makeText(this, "Wallet deleted successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, AddWalletActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to delete wallet", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddFundsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_funds, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)

        AlertDialog.Builder(this)
            .setTitle("Add Funds")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val amountStr = etAmount.text.toString().trim()
                if (amountStr.isNotEmpty()) {
                    val amount = amountStr.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        if (wallet != null) {
                            wallet!!.balance += amount
                            val success = dbHelper.updateWallet(wallet!!)
                            if (success) {
                                // Format balance
                                val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
                                currencyFormat.currency = Currency.getInstance("ZAR")
                                tvBalance.text = currencyFormat.format(wallet!!.balance).replace("ZAR", "R")

                                // Create notification for adding funds
                                val notification = Notification(
                                    0,
                                    userId,
                                    "Funds added to wallet: +R$amount",
                                    "income",
                                    amount, // Positive amount for adding funds
                                    Calendar.getInstance().time
                                )
                                dbHelper.addNotification(notification)

                                Toast.makeText(this, "Funds added successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Failed to add funds", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Amount is required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
