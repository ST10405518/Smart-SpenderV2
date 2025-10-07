package com.example.SmartSpender.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.SmartSpender.models.CategorySpending
import com.example.SmartSpender.models.Notification
import com.example.SmartSpender.models.Transaction
import com.example.SmartSpender.models.User
import com.example.SmartSpender.models.Wallet
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "SmartSpender.db"
        private const val DATABASE_VERSION = 1

        // User table
        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_USER_NAME = "name"
        private const val COLUMN_USER_EMAIL = "email"
        private const val COLUMN_USER_MOBILE = "mobile"
        private const val COLUMN_USER_DOB = "dob"
        private const val COLUMN_USER_PASSWORD = "password"

        // Wallet table
        private const val TABLE_WALLETS = "wallets"
        private const val COLUMN_WALLET_ID = "id"
        private const val COLUMN_WALLET_USER_ID = "user_id"
        private const val COLUMN_WALLET_NAME_ON_CARD = "name_on_card"
        private const val COLUMN_WALLET_CARD_NUMBER = "card_number"
        private const val COLUMN_WALLET_CVC = "cvc"
        private const val COLUMN_WALLET_EXPIRATION_DATE = "expiration_date"
        private const val COLUMN_WALLET_ZIP = "zip"
        private const val COLUMN_WALLET_BALANCE = "balance"

        // Transaction table
        private const val TABLE_TRANSACTIONS = "transactions"
        private const val COLUMN_TRANSACTION_ID = "id"
        private const val COLUMN_TRANSACTION_USER_ID = "user_id"
        private const val COLUMN_TRANSACTION_NAME = "name"
        private const val COLUMN_TRANSACTION_COMPANY = "company"
        private const val COLUMN_TRANSACTION_CATEGORY = "category"
        private const val COLUMN_TRANSACTION_AMOUNT = "amount"
        private const val COLUMN_TRANSACTION_DATE = "date"
        private const val COLUMN_TRANSACTION_PHOTO = "photo"

        // Notification table
        private const val TABLE_NOTIFICATIONS = "notifications"
        private const val COLUMN_NOTIFICATION_ID = "id"
        private const val COLUMN_NOTIFICATION_USER_ID = "user_id"
        private const val COLUMN_NOTIFICATION_MESSAGE = "message"
        private const val COLUMN_NOTIFICATION_TYPE = "type"
        private const val COLUMN_NOTIFICATION_AMOUNT = "amount"
        private const val COLUMN_NOTIFICATION_DATE = "date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_NAME TEXT,
                $COLUMN_USER_EMAIL TEXT UNIQUE,
                $COLUMN_USER_MOBILE TEXT,
                $COLUMN_USER_DOB TEXT,
                $COLUMN_USER_PASSWORD TEXT
            )
        """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_WALLETS (
                $COLUMN_WALLET_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_WALLET_USER_ID INTEGER,
                $COLUMN_WALLET_NAME_ON_CARD TEXT,
                $COLUMN_WALLET_CARD_NUMBER TEXT,
                $COLUMN_WALLET_CVC TEXT,
                $COLUMN_WALLET_EXPIRATION_DATE TEXT,
                $COLUMN_WALLET_ZIP TEXT,
                $COLUMN_WALLET_BALANCE REAL,
                FOREIGN KEY($COLUMN_WALLET_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_TRANSACTIONS (
                $COLUMN_TRANSACTION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TRANSACTION_USER_ID INTEGER,
                $COLUMN_TRANSACTION_NAME TEXT,
                $COLUMN_TRANSACTION_COMPANY TEXT,
                $COLUMN_TRANSACTION_CATEGORY TEXT,
                $COLUMN_TRANSACTION_AMOUNT REAL,
                $COLUMN_TRANSACTION_DATE TEXT,
                $COLUMN_TRANSACTION_PHOTO BLOB,
                FOREIGN KEY($COLUMN_TRANSACTION_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_NOTIFICATIONS (
                $COLUMN_NOTIFICATION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NOTIFICATION_USER_ID INTEGER,
                $COLUMN_NOTIFICATION_MESSAGE TEXT,
                $COLUMN_NOTIFICATION_TYPE TEXT,
                $COLUMN_NOTIFICATION_AMOUNT REAL,
                $COLUMN_NOTIFICATION_DATE TEXT,
                FOREIGN KEY($COLUMN_NOTIFICATION_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTIFICATIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WALLETS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    fun addUser(user: User): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_NAME, user.name)
            put(COLUMN_USER_EMAIL, user.email)
            put(COLUMN_USER_MOBILE, user.mobile)
            put(COLUMN_USER_DOB, user.dob)
            put(COLUMN_USER_PASSWORD, user.password)
        }
        return db.insert(TABLE_USERS, null, values).also { db.close() }
    }

    fun getUserByEmail(email: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_USER_EMAIL = ?",
            arrayOf(email),
            null, null, null
        )

        val user = if (cursor.moveToFirst()) {
            User(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)),
                mobile = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_MOBILE)),
                dob = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_DOB)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD))
            )
        } else null

        cursor.close()
        db.close()
        return user
    }

    fun addWallet(wallet: Wallet): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_WALLET_USER_ID, wallet.userId)
            put(COLUMN_WALLET_NAME_ON_CARD, wallet.nameOnCard)
            put(COLUMN_WALLET_CARD_NUMBER, wallet.cardNumber)
            put(COLUMN_WALLET_CVC, wallet.cvc)
            put(COLUMN_WALLET_EXPIRATION_DATE, wallet.expirationDate)
            put(COLUMN_WALLET_ZIP, wallet.zip)
            put(COLUMN_WALLET_BALANCE, wallet.balance)
        }
        val id = db.insert(TABLE_WALLETS, null, values)
        db.close()
        return id
    }

    fun updateWallet(wallet: Wallet): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_WALLET_NAME_ON_CARD, wallet.nameOnCard)
            put(COLUMN_WALLET_CARD_NUMBER, wallet.cardNumber)
            put(COLUMN_WALLET_CVC, wallet.cvc)
            put(COLUMN_WALLET_EXPIRATION_DATE, wallet.expirationDate)
            put(COLUMN_WALLET_ZIP, wallet.zip)
            put(COLUMN_WALLET_BALANCE, wallet.balance)
        }
        val rowsUpdated = db.update(
            TABLE_WALLETS,
            values,
            "$COLUMN_WALLET_ID = ?",
            arrayOf(wallet.id.toString())
        )
        return rowsUpdated > 0
    }

    fun deleteWallet(walletId: Int): Boolean {
        val db = writableDatabase
        val rowsDeleted = db.delete(
            TABLE_WALLETS,
            "$COLUMN_WALLET_ID = ?",
            arrayOf(walletId.toString())
        )
        return rowsDeleted > 0
    }

    fun getWalletByUserId(userId: Int): Wallet? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_WALLETS,
            null,
            "$COLUMN_WALLET_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        val wallet = if (cursor.moveToFirst()) {
            Wallet(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WALLET_ID)),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WALLET_USER_ID)),
                nameOnCard = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WALLET_NAME_ON_CARD)),
                cardNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WALLET_CARD_NUMBER)),
                cvc = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WALLET_CVC)),
                expirationDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WALLET_EXPIRATION_DATE)),
                zip = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WALLET_ZIP)),
                balance = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_WALLET_BALANCE))
            )
        } else null

        cursor.close()
        return wallet
    }

    fun addTransaction(transaction: Transaction): Long {
        val db = writableDatabase
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = dateFormat.format(transaction.date)
        val values = ContentValues().apply {
            put(COLUMN_TRANSACTION_USER_ID, transaction.userId)
            put(COLUMN_TRANSACTION_NAME, transaction.name)
            put(COLUMN_TRANSACTION_COMPANY, transaction.company)
            put(COLUMN_TRANSACTION_CATEGORY, transaction.category)
            put(COLUMN_TRANSACTION_AMOUNT, transaction.amount)
            put(COLUMN_TRANSACTION_DATE, dateString)
            put(COLUMN_TRANSACTION_PHOTO, transaction.photo)
        }
        val id = db.insert(TABLE_TRANSACTIONS, null, values)

        // Create notification for the transaction
        if (id > 0) {
            val notification = Notification(
                0,
                transaction.userId,
                "New expense: ${transaction.category} - ${transaction.name}",
                "expense",
                transaction.amount,
                transaction.date
            )
            addNotification(notification)

            // Check if spending limit is reached
            checkSpendingLimit(transaction.userId)
        }

        db.close()
        return id
    }

    private fun checkSpendingLimit(userId: Int) {
        val sharedPref = context.getSharedPreferences("SmartSpenderPrefs", Context.MODE_PRIVATE)
        val budget = sharedPref.getFloat("userBudget", 0.0f).toDouble()

        if (budget <= 0) return // No budget set

        val totalSpent = Math.abs(getTotalSpentByUserId(userId))
        val percentSpent = (totalSpent / budget) * 100

        // Alert at 50%, 75%, 90%, and 100% of budget
        val thresholds = listOf(50.0, 75.0, 90.0, 100.0)

        for (threshold in thresholds) {
            if (percentSpent >= threshold) {
                // Check if notification for this threshold already exists
                val existingNotification = getSpendingLimitNotification(userId, threshold)
                if (existingNotification == null) {
                    // Create spending limit notification
                    val notification = Notification(
                        0,
                        userId,
                        "You've reached ${threshold.toInt()}% of your budget (R${String.format("%.2f", totalSpent)} of R${String.format("%.2f", budget)})",
                        "warning",
                        -totalSpent,
                        Calendar.getInstance().time
                    )
                    addNotification(notification)
                }
            }
        }
    }

    private fun getSpendingLimitNotification(userId: Int, threshold: Double): Notification? {
        val db = readableDatabase
        val thresholdMessage = "You've reached ${threshold.toInt()}%"

        val query = """
            SELECT * FROM $TABLE_NOTIFICATIONS 
            WHERE $COLUMN_NOTIFICATION_USER_ID = ? AND $COLUMN_NOTIFICATION_MESSAGE LIKE ? 
            ORDER BY $COLUMN_NOTIFICATION_DATE DESC LIMIT 1
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), "%$thresholdMessage%"))

        val notification = if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_ID))
            val userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_USER_ID))
            val message = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_MESSAGE))
            val type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_TYPE))
            val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_AMOUNT))
            val dateString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_DATE))
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = try {
                dateFormat.parse(dateString) ?: Calendar.getInstance().time
            } catch (e: Exception) {
                Calendar.getInstance().time
            }

            Notification(id, userId, message, type, amount, date)
        } else null

        cursor.close()
        return notification
    }

    fun addNotification(notification: Notification): Long {
        val db = writableDatabase
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateString = dateFormat.format(notification.date)
        val values = ContentValues().apply {
            put(COLUMN_NOTIFICATION_USER_ID, notification.userId)
            put(COLUMN_NOTIFICATION_MESSAGE, notification.message)
            put(COLUMN_NOTIFICATION_TYPE, notification.type)
            put(COLUMN_NOTIFICATION_AMOUNT, notification.amount)
            put(COLUMN_NOTIFICATION_DATE, dateString)
        }
        return db.insert(TABLE_NOTIFICATIONS, null, values)
    }

    fun getNotificationsByUserId(userId: Int): ArrayList<Notification> {
        val notifications = ArrayList<Notification>()
        val db = readableDatabase
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val query = """
            SELECT * FROM $TABLE_NOTIFICATIONS 
            WHERE $COLUMN_NOTIFICATION_USER_ID = ? 
            ORDER BY $COLUMN_NOTIFICATION_DATE DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_ID))
                val userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_USER_ID))
                val message = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_MESSAGE))
                val type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_TYPE))
                val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_AMOUNT))
                val dateString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_DATE))
                val date = try {
                    dateFormat.parse(dateString) ?: Calendar.getInstance().time
                } catch (e: Exception) {
                    Calendar.getInstance().time
                }

                notifications.add(
                    Notification(
                        id = id,
                        userId = userId,
                        message = message,
                        type = type,
                        amount = amount,
                        date = date
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        return notifications
    }

    fun getTodayTransactionsByUserId(userId: Int): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val db = this.readableDatabase
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val query = """
            SELECT * FROM $TABLE_TRANSACTIONS 
            WHERE $COLUMN_TRANSACTION_USER_ID = ? AND $COLUMN_TRANSACTION_DATE = ? 
            ORDER BY $COLUMN_TRANSACTION_DATE DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), currentDate))

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID))
                val userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_USER_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_NAME))
                val company = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_COMPANY))
                val category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_CATEGORY))
                val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_AMOUNT))
                val dateString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_DATE))
                val date = dateFormat.parse(dateString)
                val photo = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_PHOTO))

                transactions.add(
                    Transaction(
                        id = id,
                        userId = userId,
                        name = name,
                        company = company,
                        category = category,
                        amount = amount,
                        date = date,
                        photo = photo
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return transactions
    }

    fun getTransactionsByPeriod(userId: Int, startDate: Date, endDate: Date): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val db = this.readableDatabase
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDateStr = dateFormat.format(startDate)
        val endDateStr = dateFormat.format(endDate)

        val query = """
            SELECT * FROM $TABLE_TRANSACTIONS 
            WHERE $COLUMN_TRANSACTION_USER_ID = ? 
            AND $COLUMN_TRANSACTION_DATE BETWEEN ? AND ?
            ORDER BY $COLUMN_TRANSACTION_DATE DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), startDateStr, endDateStr))

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID))
                val userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_USER_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_NAME))
                val company = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_COMPANY))
                val category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_CATEGORY))
                val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_AMOUNT))
                val dateString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_DATE))
                val date = dateFormat.parse(dateString)
                val photo = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_PHOTO))

                transactions.add(
                    Transaction(
                        id = id,
                        userId = userId,
                        name = name,
                        company = company,
                        category = category,
                        amount = amount,
                        date = date,
                        photo = photo
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return transactions
    }

    fun getCategorySpendingByPeriod(userId: Int, startDate: Date, endDate: Date): List<CategorySpending> {
        val categorySpending = mutableListOf<CategorySpending>()
        val db = this.readableDatabase
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDateStr = dateFormat.format(startDate)
        val endDateStr = dateFormat.format(endDate)

        val query = """
            SELECT $COLUMN_TRANSACTION_CATEGORY as category, SUM($COLUMN_TRANSACTION_AMOUNT) as total
            FROM $TABLE_TRANSACTIONS 
            WHERE $COLUMN_TRANSACTION_USER_ID = ? 
            AND $COLUMN_TRANSACTION_DATE BETWEEN ? AND ?
            GROUP BY $COLUMN_TRANSACTION_CATEGORY
            ORDER BY total ASC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), startDateStr, endDateStr))

        if (cursor.moveToFirst()) {
            do {
                val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
                val amount = cursor.getDouble(cursor.getColumnIndexOrThrow("total"))

                categorySpending.add(CategorySpending(category, amount))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return categorySpending
    }

    fun getTransactionById(transactionId: Int): Transaction? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TRANSACTIONS,
            null,
            "$COLUMN_TRANSACTION_ID = ?",
            arrayOf(transactionId.toString()),
            null, null, null
        )

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val transaction = if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID))
            val userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_USER_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_NAME))
            val company = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_COMPANY))
            val category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_CATEGORY))
            val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_AMOUNT))
            val dateString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_DATE))
            val date = dateFormat.parse(dateString)
            val photo = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_PHOTO))

            Transaction(
                id = id,
                userId = userId,
                name = name,
                company = company,
                category = category,
                amount = amount,
                date = date,
                photo = photo
            )
        } else null

        cursor.close()
        return transaction
    }

    fun getMonthlySpendingByYear(userId: Int, year: Int): Map<Int, Double> {
        val monthlySpending = mutableMapOf<Int, Double>()
        val db = this.readableDatabase

        val query = """
            SELECT 
                strftime('%m', $COLUMN_TRANSACTION_DATE) AS month,
                SUM($COLUMN_TRANSACTION_AMOUNT) AS total
            FROM 
                $TABLE_TRANSACTIONS
            WHERE 
                $COLUMN_TRANSACTION_USER_ID = ? AND strftime('%Y', $COLUMN_TRANSACTION_DATE) = ?
            GROUP BY 
                month
            ORDER BY 
                month ASC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), year.toString()))

        if (cursor.moveToFirst()) {
            do {
                val month = cursor.getString(cursor.getColumnIndexOrThrow("month")).toInt()
                val total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"))
                monthlySpending[month] = total
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return monthlySpending
    }

    fun getTotalSpentByUserId(userId: Int): Double {
        val db = readableDatabase
        var totalSpent = 0.0
        val cursor = db.query(
            TABLE_TRANSACTIONS,
            arrayOf("SUM($COLUMN_TRANSACTION_AMOUNT) as total"),
            "$COLUMN_TRANSACTION_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            totalSpent = cursor.getDouble(cursor.getColumnIndexOrThrow("total"))
        }
        cursor.close()
        return totalSpent
    }
}
