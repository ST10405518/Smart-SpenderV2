package com.example.SmartSpender

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.SmartSpender.database.DatabaseHelper
import com.example.SmartSpender.models.Transaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddSpendingActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private val PERMISSIONS_REQUEST_CODE = 100
    private val REQUIRED_PERMISSIONS = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private lateinit var etExpenseName: EditText
    private lateinit var etDate: EditText
    private lateinit var etStartTime: EditText
    private lateinit var etEndTime: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etAmount: EditText
    private lateinit var btnAddPhoto: Button
    private lateinit var ivReceiptPhoto: ImageView
    private lateinit var btnAdd: Button
    private lateinit var btnCancel: Button
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var menuButton: ImageButton
    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = 0
    private var photoUri: Uri? = null
    private var photoBitmap: Bitmap? = null
    private val PICK_IMAGE_REQUEST = 1
    private val CAPTURE_IMAGE_REQUEST = 2
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_spending)

        // Check and request permissions immediately
        checkAndRequestPermissions()

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
        etExpenseName = findViewById(R.id.etExpenseName)
        etDate = findViewById(R.id.etDate)
        etStartTime = findViewById(R.id.etStartTime)
        etEndTime = findViewById(R.id.etEndTime)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        etAmount = findViewById(R.id.etAmount)
        btnAddPhoto = findViewById(R.id.btnAddPhoto)
        ivReceiptPhoto = findViewById(R.id.ivReceiptPhoto)
        btnAdd = findViewById(R.id.btnAdd)
        btnCancel = findViewById(R.id.btnCancel)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        menuButton = findViewById(R.id.btnMenu)

        // Initialize database helper
        dbHelper = DatabaseHelper(this)

        // Set up bottom navigation
        bottomNavigation.setOnNavigationItemSelectedListener(this)
        bottomNavigation.menu.findItem(R.id.navigation_add).isChecked = true

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

        // Set up category spinner
        val categories = resources.getStringArray(R.array.expense_categories)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // Set up date picker
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        etDate.setOnClickListener {
            DatePickerDialog(
                this, dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Set up time pickers
        etStartTime.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val time = String.format("%02d:%02d", hourOfDay, minute)
                    etStartTime.setText(time)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        etEndTime.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val time = String.format("%02d:%02d", hourOfDay, minute)
                    etEndTime.setText(time)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        // Set current date as default
        updateDateInView()

        // Set up Add Photo button
        btnAddPhoto.setOnClickListener {
            if (hasRequiredPermissions()) {
                showImageSourceDialog()
            } else {
                Toast.makeText(this, "Please grant camera and storage permissions", Toast.LENGTH_LONG).show()
                checkAndRequestPermissions()
            }
        }

        // Set up Add button
        btnAdd.setOnClickListener {
            saveTransaction()
        }

        // Set up Cancel button
        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = ArrayList<String>()
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // All permissions granted
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions not granted. Some features may not work.", Toast.LENGTH_LONG).show()
                // Show dialog explaining why permissions are needed
                AlertDialog.Builder(this)
                    .setTitle("Permissions Required")
                    .setMessage("This app needs camera and storage permissions to upload photos. Please grant these permissions in Settings.")
                    .setPositiveButton("Settings") { _, _ ->
                        // Open app settings
                        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun updateDateInView() {
        val format = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        etDate.setText(sdf.format(calendar.time))
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Photo")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> {
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (takePictureIntent.resolveActivity(packageManager) != null) {
                        startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST)
                    } else {
                        Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show()
                    }
                }
                options[item] == "Choose from Gallery" -> {
                    val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(pickPhotoIntent, PICK_IMAGE_REQUEST)
                }
                options[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    photoUri = data?.data
                    try {
                        photoBitmap = MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
                        ivReceiptPhoto.setImageBitmap(photoBitmap)
                        ivReceiptPhoto.visibility = View.VISIBLE
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this, "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                CAPTURE_IMAGE_REQUEST -> {
                    val extras = data?.extras
                    photoBitmap = extras?.get("data") as? Bitmap
                    if (photoBitmap != null) {
                        ivReceiptPhoto.setImageBitmap(photoBitmap)
                        ivReceiptPhoto.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun saveTransaction() {
        try {
            val name = etExpenseName.text.toString().trim()
            val date = etDate.text.toString().trim()
            val startTime = etStartTime.text.toString().trim()
            val endTime = etEndTime.text.toString().trim()
            val category = spinnerCategory.selectedItem.toString()
            val amountStr = etAmount.text.toString().trim()

            if (name.isEmpty()) {
                etExpenseName.error = "Expense name is required"
                etExpenseName.requestFocus()
                return
            }

            if (date.isEmpty()) {
                etDate.error = "Date is required"
                etDate.requestFocus()
                return
            }

            if (startTime.isEmpty()) {
                etStartTime.error = "Start time is required"
                etStartTime.requestFocus()
                return
            }

            if (endTime.isEmpty()) {
                etEndTime.error = "End time is required"
                etEndTime.requestFocus()
                return
            }

            if (amountStr.isEmpty()) {
                etAmount.error = "Amount is required"
                etAmount.requestFocus()
                return
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                etAmount.error = "Enter a valid amount"
                etAmount.requestFocus()
                return
            }

            // Format description with times
            val description = "$name ($startTime - $endTime)"

            // Convert bitmap to byte array if photo exists
            var photoBytes: ByteArray? = null
            if (photoBitmap != null) {
                val stream = ByteArrayOutputStream()
                photoBitmap?.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                photoBytes = stream.toByteArray()
            }

            // Parse date
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val parsedDate = dateFormat.parse(date) ?: Calendar.getInstance().time

            // Create transaction object with negative amount (expense)
            val transaction = Transaction(
                0,
                userId,
                description,
                category,
                category,
                -amount, // Make amount negative for expenses
                parsedDate,
                photoBytes
            )

            // Save transaction to database
            val transactionId = dbHelper.addTransaction(transaction)

            if (transactionId > 0) {
                // Update wallet balance
                val wallet = dbHelper.getWalletByUserId(userId)
                if (wallet != null) {
                    wallet.balance -= amount
                    dbHelper.updateWallet(wallet)
                }

                Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to add expense", Toast.LENGTH_SHORT).show()
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
