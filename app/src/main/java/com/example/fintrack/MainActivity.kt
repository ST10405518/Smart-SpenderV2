package com.example.SmartSpender

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.SmartSpender.adapters.TransactionAdapter
import com.example.SmartSpender.database.DatabaseHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

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
    private var budget: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        applyDarkMode()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get user info from SharedPreferences
        val sharedPref = getSharedPreferences("SmartSpenderPrefs", MODE_PRIVATE)
        userId = sharedPref.getInt("userId", 0)
        budget = sharedPref.getFloat("userBudget", 0.0f).toDouble()

        if (userId == 0) {
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

        dbHelper = DatabaseHelper(this)

        // Setup RecyclerView
        rvTransactions.layoutManager = LinearLayoutManager(this)
        rvTransactions.isNestedScrollingEnabled = false
        transactionAdapter = TransactionAdapter(this, ArrayList())
        rvTransactions.adapter = transactionAdapter

        // Setup bottom navigation
        bottomNavigation.setOnNavigationItemSelectedListener(this)
        bottomNavigation.menu.findItem(R.id.navigation_home).isChecked = true

        // FAB click
        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddSpendingActivity::class.java))
        }

        // Popup menu
        menuButton.setOnClickListener {
            val popupMenu = PopupMenu(this, menuButton)
            popupMenu.menuInflater.inflate(R.menu.main_menu, popupMenu.menu)

            val darkModeItem = popupMenu.menu.findItem(R.id.menu_dark_mode)
            darkModeItem.title = if (isDarkModeEnabled()) "Light Mode" else "Dark Mode"

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_profile -> {
                        startActivity(Intent(this, ProfileActivity::class.java))
                        true
                    }
                    R.id.menu_settings -> {
                        startActivity(Intent(this, SettingsActivity::class.java))
                        true
                    }
                    R.id.menu_expense_report -> {
                        startActivity(Intent(this, ExpenseReportActivity::class.java))
                        true
                    }
                    R.id.menu_dark_mode -> {
                        toggleDarkMode()
                        true
                    }
                    R.id.menu_logout -> {
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

        // Prompt for budget if not set
        if (budget <= 0) showBudgetDialog()

        // Load data
        loadData()
    }

    private fun applyDarkMode() {
        val sharedPreferences = getSharedPreferences("SmartSpenderPrefs", Context.MODE_PRIVATE)
        val nightMode = sharedPreferences.getBoolean("nightMode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (nightMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun toggleDarkMode() {
        val sharedPreferences = getSharedPreferences("SmartSpenderPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val currentlyDark = isDarkModeEnabled()

        if (currentlyDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            editor.putBoolean("nightMode", false)
            Toast.makeText(this, "Light Mode Enabled", Toast.LENGTH_SHORT).show()
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            editor.putBoolean("nightMode", true)
            Toast.makeText(this, "Dark Mode Enabled", Toast.LENGTH_SHORT).show()
        }
        editor.apply()
        recreate()
    }

    private fun isDarkModeEnabled(): Boolean {
        val sharedPreferences = getSharedPreferences("SmartSpenderPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("nightMode", false)
    }

    private fun showBudgetDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_set_budget, null)
        val etBudget = dialogView.findViewById<EditText>(R.id.etBudget)
        if (budget > 0) etBudget.setText(budget.toString())

        AlertDialog.Builder(this)
            .setTitle("Set Monthly Budget")
            .setView(dialogView)
            .setPositiveButton("SAVE") { _, _ ->
                val budgetStr = etBudget.text.toString().trim()
                val newBudget = budgetStr.toDoubleOrNull()
                if (newBudget != null && newBudget > 0) {
                    with(getSharedPreferences("SmartSpenderPrefs", MODE_PRIVATE).edit()) {
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
            .setNegativeButton("CANCEL", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadData()
        checkGamificationRewards()
    }

    private fun loadData() {
        val wallet = dbHelper.getWalletByUserId(userId)
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        currencyFormat.currency = Currency.getInstance("ZAR")
        tvBalance.text = wallet?.balance?.let { currencyFormat.format(it).replace("ZAR", "R") } ?: "R 0.00"

        val totalSpent = dbHelper.getTotalSpentByUserId(userId)
        val spendingPercentage = if (budget > 0) (Math.abs(totalSpent) / budget * 100).toInt() else 0
        tvSpendingPercentage.text = "$spendingPercentage%"
        progressSpending.progress = spendingPercentage

        val formattedSpent = currencyFormat.format(Math.abs(totalSpent)).replace("ZAR", "R")
        val formattedBudget = currencyFormat.format(budget).replace("ZAR", "R")
        tvTotalSpent.text = "$formattedSpent of $formattedBudget"

        val todayTransactions = dbHelper.getTodayTransactionsByUserId(userId)
        transactionAdapter.updateTransactions(todayTransactions)
    }

    private fun checkGamificationRewards() {
        val sharedPref = getSharedPreferences("SmartSpenderPrefs", MODE_PRIVATE)
        val totalSpent = dbHelper.getTotalSpentByUserId(userId)
        val spendingPercentage = if (budget > 0) (Math.abs(totalSpent) / budget * 100).toInt() else 0

        // Streak and XP checks
        checkAndUpdateStreak()
        val todayTransactions = dbHelper.getTodayTransactionsByUserId(userId)
        if (budget > 0 && spendingPercentage <= 50 && !sharedPref.getBoolean("reward_under_budget", false)) {
            awardXP(50, "Budget Master")
            sharedPref.edit().putBoolean("reward_under_budget", true).apply()
            showEnhancedToast("Budget Master!", "+50 XP", "Amazing discipline!")
        }

        if (todayTransactions.isNotEmpty() && !sharedPref.getBoolean("reward_first_transaction", false)) {
            awardXP(15, "First Step")
            sharedPref.edit().putBoolean("reward_first_transaction", true).apply()
            showEnhancedToast("First Step!", "+15 XP", "First transaction logged!")
        }
    }

    private fun checkAndUpdateStreak() {
        val sharedPref = getSharedPreferences("SmartSpenderPrefs", MODE_PRIVATE)
        val lastLoginDate = sharedPref.getString("last_login_date", "")
        val currentStreak = sharedPref.getInt("userStreak", 0)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (lastLoginDate != today) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            val newStreak = if (lastLoginDate == yesterday) currentStreak + 1 else 1

            sharedPref.edit().putInt("userStreak", newStreak).putString("last_login_date", today).apply()

            when (newStreak) {
                3 -> awardXP(25, "3-Day Streak")
                7 -> awardXP(50, "Weekly Streak")
                30 -> awardXP(200, "Monthly Dedication")
            }
        }
    }

    private fun awardXP(xpAmount: Int, achievement: String) {
        val sharedPref = getSharedPreferences("SmartSpenderPrefs", MODE_PRIVATE)
        val newXP = sharedPref.getInt("userXP", 0) + xpAmount
        sharedPref.edit().putInt("userXP", newXP).putString("recentAchievement", achievement).apply()
        val oldLevel = calculateLevel(newXP - xpAmount)
        val newLevel = calculateLevel(newXP)
        if (newLevel > oldLevel) showEnhancedToast("LEVEL UP!", "Level $newLevel", "Congratulations!")
    }

    private fun calculateLevel(xp: Int) = when {
        xp >= 5000 -> 10
        xp >= 2500 -> 9
        xp >= 1500 -> 8
        xp >= 1000 -> 7
        xp >= 750 -> 6
        xp >= 500 -> 5
        xp >= 300 -> 4
        xp >= 150 -> 3
        xp >= 50 -> 2
        else -> 1
    }

    private fun showEnhancedToast(title: String, points: String, message: String) {
        val toast = Toast.makeText(this, "🎉 $title\n$points\n$message", Toast.LENGTH_LONG)
        val toastView = toast.view
        toastView?.setBackgroundColor(resources.getColor(android.R.color.white, theme))
        val text = toastView?.findViewById<TextView>(android.R.id.message)
        text?.setTextColor(resources.getColor(android.R.color.black, theme))
        text?.textSize = 14f
        toast.show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_home -> return true
            R.id.navigation_stats -> startActivity(Intent(this, StatisticsActivity::class.java))
            R.id.navigation_add -> startActivity(Intent(this, AddSpendingActivity::class.java))
            R.id.navigation_wallet -> {
                val wallet = dbHelper.getWalletByUserId(userId)
                if (wallet != null) startActivity(Intent(this, EditWalletActivity::class.java))
                else startActivity(Intent(this, AddWalletActivity::class.java))
            }
            R.id.navigation_notifications -> startActivity(Intent(this, NotificationActivity::class.java))
        }
        return true
    }
}
