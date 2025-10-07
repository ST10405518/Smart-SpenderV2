package com.example.SmartSpender

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.SmartSpender.adapters.TransactionAdapter
import com.example.SmartSpender.database.DatabaseHelper
import com.example.SmartSpender.models.Transaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import android.widget.ImageButton
import android.widget.PopupMenu
import java.text.NumberFormat
import java.util.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var tvBalance: TextView
    private lateinit var tvSpendingPercentage: TextView
    private lateinit var tvTotalSpent: TextView
    private lateinit var progressSpending: CircularProgressIndicator
    private lateinit var rvTransactions: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var menuButton: ImageButton
    private lateinit var scrollView: NestedScrollView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var transactionAdapter: TransactionAdapter
    private var userId: Int = 0
    private var budget: Double = 0.0 // Default budget set to 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get user ID from shared preferences
        val sharedPref = getSharedPreferences("SmartSpenderPrefs", MODE_PRIVATE)
        userId = sharedPref.getInt("userId", 0)
        budget = sharedPref.getFloat("userBudget", 0.0f).toDouble() // Default to 0

        if (userId == 0) {
            // User not logged in, redirect to login
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialize views
        tvBalance = findViewById(R.id.tvBalance)
        tvSpendingPercentage = findViewById(R.id.tvSpendingPercentage)
        tvTotalSpent = findViewById(R.id.tvTotalSpent)
        progressSpending = findViewById(R.id.progressSpending)
        rvTransactions = findViewById(R.id.rvTransactions)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        fabAdd = findViewById(R.id.fabAdd)
        menuButton = findViewById(R.id.btnMenu)
        scrollView = findViewById(R.id.scrollView)

        // Initialize database helper
        dbHelper = DatabaseHelper(this)

        // Set up RecyclerView
        rvTransactions.layoutManager = LinearLayoutManager(this)
        rvTransactions.isNestedScrollingEnabled = false
        transactionAdapter = TransactionAdapter(this, ArrayList())
        rvTransactions.adapter = transactionAdapter

        // Set up bottom navigation
        bottomNavigation.setOnNavigationItemSelectedListener(this)
        bottomNavigation.menu.findItem(R.id.navigation_home).isChecked = true

        // Set up FAB
        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddSpendingActivity::class.java))
        }

        // Set up menu button
        menuButton.setOnClickListener {
            // Show menu options
            val popupMenu = PopupMenu(this, menuButton)
            popupMenu.menuInflater.inflate(R.menu.main_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_profile -> {
                        // Handle profile click
                        Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.menu_settings -> {
                        // Show budget setting dialog
                        showBudgetDialog()
                        true
                    }
                    R.id.menu_expense_report -> {
                        startActivity(Intent(this, ExpenseReportActivity::class.java))
                        true
                    }
                    R.id.menu_logout -> {
                        // Handle logout
                        val sharedPref = getSharedPreferences("SmartSpenderPrefs", MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            clear()
                            apply()
                        }
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        // If budget is not set, prompt user to set it
        if (budget <= 0) {
            showBudgetDialog()
        }

        // Load data
        loadData()
    }

    private fun showBudgetDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_set_budget, null)
        val etBudget = dialogView.findViewById<EditText>(R.id.etBudget)

        // Set current budget if it's greater than 0
        if (budget > 0) {
            etBudget.setText(budget.toString())
        }

        AlertDialog.Builder(this)
            .setTitle("Set Budget")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val budgetStr = etBudget.text.toString().trim()
                if (budgetStr.isNotEmpty()) {
                    val newBudget = budgetStr.toDoubleOrNull()
                    if (newBudget != null && newBudget > 0) {
                        // Save budget to shared preferences
                        val sharedPref = getSharedPreferences("SmartSpenderPrefs", MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putFloat("userBudget", newBudget.toFloat())
                            apply()
                        }
                        budget = newBudget
                        loadData()
                        Toast.makeText(this, "Budget updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Enter a valid budget amount", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        // Load wallet balance
        val wallet = dbHelper.getWalletByUserId(userId)
        if (wallet != null) {
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            currencyFormat.currency = Currency.getInstance("ZAR")
            tvBalance.text = currencyFormat.format(wallet.balance).replace("ZAR", "R")
        } else {
            tvBalance.text = "R 0.00"
        }

        // Load spending data
        val totalSpent = dbHelper.getTotalSpentByUserId(userId)
        val spendingPercentage = if (budget > 0) (Math.abs(totalSpent) / budget * 100).toInt() else 0

        tvSpendingPercentage.text = "$spendingPercentage%"
        progressSpending.progress = spendingPercentage

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        currencyFormat.currency = Currency.getInstance("ZAR")
        val formattedSpent = currencyFormat.format(Math.abs(totalSpent)).replace("ZAR", "R")
        val formattedBudget = currencyFormat.format(budget).replace("ZAR", "R")
        tvTotalSpent.text = "$formattedSpent Of $formattedBudget"

        // Load today's transactions
        val todayTransactions = dbHelper.getTodayTransactionsByUserId(userId)
        transactionAdapter.updateTransactions(todayTransactions)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_home -> {
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
                val wallet = dbHelper.getWalletByUserId(userId)
                if (wallet != null) {
                    startActivity(Intent(this, EditWalletActivity::class.java))
                } else {
                    startActivity(Intent(this, AddWalletActivity::class.java))
                }
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
