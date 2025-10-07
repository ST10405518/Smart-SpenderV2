package com.example.SmartSpender

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.SmartSpender.adapters.TransactionAdapter
import com.example.SmartSpender.database.DatabaseHelper
import com.example.SmartSpender.models.Transaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ExpenseReportActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var etStartDate: EditText
    private lateinit var etEndDate: EditText
    private lateinit var btnGenerateReport: Button
    private lateinit var tvTotalExpenses: TextView
    private lateinit var rvExpenseReport: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var menuButton: ImageButton
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var transactionAdapter: TransactionAdapter
    private var userId: Int = 0
    private val startCalendar = Calendar.getInstance()
    private val endCalendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_report)

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
        etStartDate = findViewById(R.id.etStartDate)
        etEndDate = findViewById(R.id.etEndDate)
        btnGenerateReport = findViewById(R.id.btnGenerateReport)
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses)
        rvExpenseReport = findViewById(R.id.rvExpenseReport)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        menuButton = findViewById(R.id.btnMenu)

        // Initialize database helper
        dbHelper = DatabaseHelper(this)

        // Set up RecyclerView
        rvExpenseReport.layoutManager = LinearLayoutManager(this)
        transactionAdapter = TransactionAdapter(this, ArrayList())
        transactionAdapter.setOnItemClickListener(object : TransactionAdapter.OnItemClickListener {
            override fun onItemClick(transaction: Transaction) {
                if (transaction.photo != null) {
                    showPhotoDialog(transaction)
                }
            }
        })
        rvExpenseReport.adapter = transactionAdapter

        // Set up bottom navigation
        bottomNavigation.setOnNavigationItemSelectedListener(this)

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
                        // Handle settings click
                        Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.menu_expense_report -> {
                        // Already in expense report
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

        // Set up date pickers
        val startDateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            startCalendar.set(Calendar.YEAR, year)
            startCalendar.set(Calendar.MONTH, monthOfYear)
            startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateStartDateInView()
        }

        val endDateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            endCalendar.set(Calendar.YEAR, year)
            endCalendar.set(Calendar.MONTH, monthOfYear)
            endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateEndDateInView()
        }

        etStartDate.setOnClickListener {
            DatePickerDialog(
                this, startDateSetListener,
                startCalendar.get(Calendar.YEAR),
                startCalendar.get(Calendar.MONTH),
                startCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        etEndDate.setOnClickListener {
            DatePickerDialog(
                this, endDateSetListener,
                endCalendar.get(Calendar.YEAR),
                endCalendar.get(Calendar.MONTH),
                endCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Set default dates (current month)
        startCalendar.set(Calendar.DAY_OF_MONTH, 1)
        updateStartDateInView()
        updateEndDateInView()

        // Set up Generate Report button
        btnGenerateReport.setOnClickListener {
            generateReport()
        }

        // Generate initial report
        generateReport()
    }

    private fun updateStartDateInView() {
        etStartDate.setText(dateFormat.format(startCalendar.time))
    }

    private fun updateEndDateInView() {
        etEndDate.setText(dateFormat.format(endCalendar.time))
    }

    private fun generateReport() {
        val startDate = startCalendar.time
        val endDate = endCalendar.time

        if (startDate.after(endDate)) {
            Toast.makeText(this, "Start date cannot be after end date", Toast.LENGTH_SHORT).show()
            return
        }

        val transactions = dbHelper.getTransactionsByPeriod(userId, startDate, endDate)
        transactionAdapter.updateTransactions(transactions)

        // Calculate total expenses
        var totalExpenses = 0.0
        for (transaction in transactions) {
            totalExpenses += transaction.amount
        }

        // Format and display total expenses
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        currencyFormat.currency = Currency.getInstance("ZAR")
        val formattedTotal = currencyFormat.format(Math.abs(totalExpenses)).replace("ZAR", "R")
        tvTotalExpenses.text = "Total Expenses: $formattedTotal"
    }

    private fun showPhotoDialog(transaction: Transaction) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_view_photo, null)
        val ivPhoto = dialogView.findViewById<ImageView>(R.id.ivPhoto)

        // Convert byte array to bitmap
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(
            transaction.photo, 0, transaction.photo!!.size
        )
        ivPhoto.setImageBitmap(bitmap)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Receipt Photo")
            .setView(dialogView)
            .setPositiveButton("Close", null)
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
