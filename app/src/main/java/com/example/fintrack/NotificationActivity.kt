package com.example.SmartSpender

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.SmartSpender.adapters.NotificationAdapter
import com.example.SmartSpender.database.DatabaseHelper
import com.example.SmartSpender.models.Notification
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class NotificationActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var rvNotifications: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var menuButton: ImageButton
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var notificationAdapter: NotificationAdapter
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

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
        rvNotifications = findViewById(R.id.rvNotifications)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        fabAdd = findViewById(R.id.fabAdd)
        menuButton = findViewById(R.id.btnMenu)

        // Initialize database helper
        dbHelper = DatabaseHelper(this)

        // Set up RecyclerView
        rvNotifications.layoutManager = LinearLayoutManager(this)
        notificationAdapter = NotificationAdapter(this, ArrayList())
        rvNotifications.adapter = notificationAdapter

        // Set up bottom navigation
        bottomNavigation.setOnNavigationItemSelectedListener(this)
        bottomNavigation.menu.findItem(R.id.navigation_notifications).isChecked = true

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
                        // Handle settings click
                        Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
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

        // Load notifications
        loadNotifications()
    }

    private fun loadNotifications() {
        // Get actual notifications from database
        val notifications = dbHelper.getNotificationsByUserId(userId)
        
        // If no notifications, create a welcome notification
        if (notifications.isEmpty()) {
            val welcomeNotification = Notification(
                0,
                userId,
                "Welcome to SmartSpender! Start tracking your expenses.",
                "info",
                0.0,
                Calendar.getInstance().time
            )
            notifications.add(welcomeNotification)
        }
        
        notificationAdapter.updateNotifications(notifications)
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
                return true
            }
        }
        return false
    }
}
