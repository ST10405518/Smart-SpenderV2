package com.example.SmartSpender.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.SmartSpender.models.User
import java.text.SimpleDateFormat
import java.util.*
import com.example.SmartSpender.models.Transaction
import com.example.SmartSpender.models.Wallet
import com.example.SmartSpender.models.Notification
import com.example.SmartSpender.models.CategorySpending

class DatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "SmartSpender.db"
        private const val DATABASE_VERSION = 1

        // USERS
        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_USER_NAME = "name"
        private const val COLUMN_USER_EMAIL = "email"
        private const val COLUMN_USER_MOBILE = "mobile"
        private const val COLUMN_USER_DOB = "dob"
        private const val COLUMN_USER_PASSWORD = "password"

        // WALLETS
        private const val TABLE_WALLETS = "wallets"
        private const val COLUMN_WALLET_ID = "id"
        private const val COLUMN_WALLET_USER_ID = "user_id"
        private const val COLUMN_WALLET_NAME_ON_CARD = "name_on_card"
        private const val COLUMN_WALLET_CARD_NUMBER = "card_number"
        private const val COLUMN_WALLET_CVC = "cvc"
        private const val COLUMN_WALLET_EXPIRATION_DATE = "expiration_date"
        private const val COLUMN_WALLET_ZIP = "zip"
        private const val COLUMN_WALLET_BALANCE = "balance"

        // TRANSACTIONS
        private const val TABLE_TRANSACTIONS = "transactions"
        private const val COLUMN_TRANSACTION_ID = "id"
        private const val COLUMN_TRANSACTION_USER_ID = "user_id"
        private const val COLUMN_TRANSACTION_NAME = "name"
        private const val COLUMN_TRANSACTION_COMPANY = "company"
        private const val COLUMN_TRANSACTION_CATEGORY = "category"
        private const val COLUMN_TRANSACTION_AMOUNT = "amount"
        private const val COLUMN_TRANSACTION_DATE = "date"
        private const val COLUMN_TRANSACTION_PHOTO = "photo"

        // NOTIFICATIONS
        private const val TABLE_NOTIFICATIONS = "notifications"
        private const val COLUMN_NOTIFICATION_ID = "id"
        private const val COLUMN_NOTIFICATION_USER_ID = "user_id"
        private const val COLUMN_NOTIFICATION_MESSAGE = "message"
        private const val COLUMN_NOTIFICATION_TYPE = "type"
        private const val COLUMN_NOTIFICATION_AMOUNT = "amount"
        private const val COLUMN_NOTIFICATION_DATE = "date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        try {
            // Users table
            db.execSQL("""
                CREATE TABLE $TABLE_USERS (
                    $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_USER_NAME TEXT NOT NULL,
                    $COLUMN_USER_EMAIL TEXT UNIQUE NOT NULL,
                    $COLUMN_USER_MOBILE TEXT,
                    $COLUMN_USER_DOB TEXT,
                    $COLUMN_USER_PASSWORD TEXT NOT NULL
                )
            """.trimIndent())

            // Wallets table
            db.execSQL("""
                CREATE TABLE $TABLE_WALLETS (
                    $COLUMN_WALLET_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_WALLET_USER_ID INTEGER NOT NULL,
                    $COLUMN_WALLET_NAME_ON_CARD TEXT NOT NULL,
                    $COLUMN_WALLET_CARD_NUMBER TEXT NOT NULL,
                    $COLUMN_WALLET_CVC TEXT NOT NULL,
                    $COLUMN_WALLET_EXPIRATION_DATE TEXT NOT NULL,
                    $COLUMN_WALLET_ZIP TEXT,
                    $COLUMN_WALLET_BALANCE REAL DEFAULT 0.0,
                    FOREIGN KEY($COLUMN_WALLET_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
                )
            """.trimIndent())

            // Transactions table
            db.execSQL("""
                CREATE TABLE $TABLE_TRANSACTIONS (
                    $COLUMN_TRANSACTION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_TRANSACTION_USER_ID INTEGER NOT NULL,
                    $COLUMN_TRANSACTION_NAME TEXT NOT NULL,
                    $COLUMN_TRANSACTION_COMPANY TEXT,
                    $COLUMN_TRANSACTION_CATEGORY TEXT NOT NULL,
                    $COLUMN_TRANSACTION_AMOUNT REAL NOT NULL,
                    $COLUMN_TRANSACTION_DATE TEXT NOT NULL,
                    $COLUMN_TRANSACTION_PHOTO BLOB,
                    FOREIGN KEY($COLUMN_TRANSACTION_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
                )
            """.trimIndent())

            // Notifications table
            db.execSQL("""
                CREATE TABLE $TABLE_NOTIFICATIONS (
                    $COLUMN_NOTIFICATION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_NOTIFICATION_USER_ID INTEGER NOT NULL,
                    $COLUMN_NOTIFICATION_MESSAGE TEXT NOT NULL,
                    $COLUMN_NOTIFICATION_TYPE TEXT NOT NULL,
                    $COLUMN_NOTIFICATION_AMOUNT REAL DEFAULT 0.0,
                    $COLUMN_NOTIFICATION_DATE TEXT NOT NULL,
                    FOREIGN KEY($COLUMN_NOTIFICATION_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
                )
            """.trimIndent())

            // Create indexes for better performance
            db.execSQL("CREATE INDEX idx_transactions_user_date ON $TABLE_TRANSACTIONS($COLUMN_TRANSACTION_USER_ID, $COLUMN_TRANSACTION_DATE)")
            db.execSQL("CREATE INDEX idx_transactions_user_category ON $TABLE_TRANSACTIONS($COLUMN_TRANSACTION_USER_ID, $COLUMN_TRANSACTION_CATEGORY)")
            db.execSQL("CREATE INDEX idx_wallets_user ON $TABLE_WALLETS($COLUMN_WALLET_USER_ID)")
            db.execSQL("CREATE INDEX idx_notifications_user ON $TABLE_NOTIFICATIONS($COLUMN_NOTIFICATION_USER_ID)")

        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error creating database", e)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTIFICATIONS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_WALLETS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
            onCreate(db)
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error upgrading database", e)
        }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    // --- USERS ---
    fun addUser(user: User): Long {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_USER_NAME, user.name)
                put(COLUMN_USER_EMAIL, user.email)
                put(COLUMN_USER_MOBILE, user.mobile)
                put(COLUMN_USER_DOB, user.dob)
                put(COLUMN_USER_PASSWORD, user.password)
            }
            db.insert(TABLE_USERS, null, values)
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error adding user", e)
            -1
        } finally {
            db.close()
        }
    }

    fun getUserByEmail(email: String): User? {
        val db = readableDatabase
        return try {
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
            user
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error getting user by email", e)
            null
        } finally {
            db.close()
        }
    }

    // --- WALLETS ---
    fun addWallet(wallet: Wallet): Long {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_WALLET_USER_ID, wallet.userId)
                put(COLUMN_WALLET_NAME_ON_CARD, wallet.nameOnCard)
                put(COLUMN_WALLET_CARD_NUMBER, wallet.cardNumber)
                put(COLUMN_WALLET_CVC, wallet.cvc)
                put(COLUMN_WALLET_EXPIRATION_DATE, wallet.expirationDate)
                put(COLUMN_WALLET_ZIP, wallet.zip)
                put(COLUMN_WALLET_BALANCE, wallet.balance)
            }
            db.insert(TABLE_WALLETS, null, values)
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error adding wallet", e)
            -1
        } finally {
            db.close()
        }
    }

    fun getWalletByUserId(userId: Int): Wallet? {
        val db = readableDatabase
        return try {
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
            wallet
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error getting wallet by user ID", e)
            null
        } finally {
            db.close()
        }
    }

    // --- GET TOTAL SPENT ---
    fun getTotalSpentByUserId(userId: Int): Double {
        val db = readableDatabase
        return try {
            val query = """
                SELECT SUM($COLUMN_TRANSACTION_AMOUNT) 
                FROM $TABLE_TRANSACTIONS 
                WHERE $COLUMN_TRANSACTION_USER_ID = ? AND $COLUMN_TRANSACTION_AMOUNT < 0
            """.trimIndent()

            val cursor = db.rawQuery(query, arrayOf(userId.toString()))
            var totalSpent = 0.0

            if (cursor.moveToFirst()) {
                totalSpent = cursor.getDouble(0)
            }

            cursor.close()
            totalSpent
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error getting total spent", e)
            0.0
        } finally {
            db.close()
        }
    }

    // --- DELETE WALLET ---
    fun deleteWallet(walletId: Int): Boolean {
        val db = writableDatabase
        return try {
            val rowsAffected = db.delete(
                TABLE_WALLETS,
                "$COLUMN_WALLET_ID = ?",
                arrayOf(walletId.toString())
            )
            rowsAffected > 0
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error deleting wallet", e)
            false
        } finally {
            db.close()
        }
    }

    // --- UPDATE WALLET ---
    fun updateWallet(wallet: Wallet): Boolean {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_WALLET_NAME_ON_CARD, wallet.nameOnCard)
                put(COLUMN_WALLET_CARD_NUMBER, wallet.cardNumber)
                put(COLUMN_WALLET_CVC, wallet.cvc)
                put(COLUMN_WALLET_EXPIRATION_DATE, wallet.expirationDate)
                put(COLUMN_WALLET_ZIP, wallet.zip)
                put(COLUMN_WALLET_BALANCE, wallet.balance)
            }
            val rowsAffected = db.update(
                TABLE_WALLETS,
                values,
                "$COLUMN_WALLET_ID = ?",
                arrayOf(wallet.id.toString())
            )
            rowsAffected > 0
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error updating wallet", e)
            false
        } finally {
            db.close()
        }
    }

    // --- UPDATE WALLET BALANCE ---
    fun updateWalletBalance(userId: Int, newBalance: Double): Boolean {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_WALLET_BALANCE, newBalance)
            }
            val rowsAffected = db.update(
                TABLE_WALLETS,
                values,
                "$COLUMN_WALLET_USER_ID = ?",
                arrayOf(userId.toString())
            )
            rowsAffected > 0
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error updating wallet balance", e)
            false
        } finally {
            db.close()
        }
    }

    // --- TRANSACTIONS ---
    fun addTransaction(transaction: Transaction): Long {
        val db = writableDatabase
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val values = ContentValues().apply {
                put(COLUMN_TRANSACTION_USER_ID, transaction.userId)
                put(COLUMN_TRANSACTION_NAME, transaction.name)
                put(COLUMN_TRANSACTION_COMPANY, transaction.company)
                put(COLUMN_TRANSACTION_CATEGORY, transaction.category)
                put(COLUMN_TRANSACTION_AMOUNT, transaction.amount)
                put(COLUMN_TRANSACTION_DATE, dateFormat.format(transaction.date))
                put(COLUMN_TRANSACTION_PHOTO, transaction.photo)
            }
            db.insert(TABLE_TRANSACTIONS, null, values)
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error adding transaction", e)
            -1
        } finally {
            db.close()
        }
    }

    fun getTransactionsByPeriod(userId: Int, startDate: Date, endDate: Date): ArrayList<Transaction> {
        val transactions = ArrayList<Transaction>()
        val db = readableDatabase
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cursor = db.rawQuery(
                "SELECT * FROM $TABLE_TRANSACTIONS WHERE $COLUMN_TRANSACTION_USER_ID = ? AND $COLUMN_TRANSACTION_DATE BETWEEN ? AND ? ORDER BY $COLUMN_TRANSACTION_DATE DESC",
                arrayOf(userId.toString(), dateFormat.format(startDate), dateFormat.format(endDate))
            )

            if (cursor.moveToFirst()) {
                do {
                    transactions.add(Transaction(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID)),
                        userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_USER_ID)),
                        name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_NAME)),
                        company = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_COMPANY)),
                        category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_CATEGORY)),
                        amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_AMOUNT)),
                        date = dateFormat.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_DATE))) ?: Date(),
                        photo = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_PHOTO))
                    ))
                } while (cursor.moveToNext())
            }
            cursor.close()
            transactions
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error getting transactions by period", e)
            transactions
        } finally {
            db.close()
        }
    }

    // --- STATISTICS METHODS ---
    fun getMonthlySpendingByYear(userId: Int, year: Int): Map<Int, Double> {
        val monthlySpending = mutableMapOf<Int, Double>()
        val db = readableDatabase

        return try {
            val query = """
                SELECT strftime('%m', $COLUMN_TRANSACTION_DATE) as month, 
                       SUM($COLUMN_TRANSACTION_AMOUNT) as total 
                FROM $TABLE_TRANSACTIONS 
                WHERE $COLUMN_TRANSACTION_USER_ID = ? 
                AND strftime('%Y', $COLUMN_TRANSACTION_DATE) = ? 
                GROUP BY month 
                ORDER BY month
            """.trimIndent()

            val cursor = db.rawQuery(query, arrayOf(userId.toString(), year.toString()))

            while (cursor.moveToNext()) {
                val month = cursor.getString(0).toInt()
                val total = cursor.getDouble(1)
                monthlySpending[month] = total
            }

            cursor.close()
            monthlySpending
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error getting monthly spending", e)
            monthlySpending
        } finally {
            db.close()
        }
    }

    fun getCategorySpendingByPeriod(userId: Int, startDate: Date, endDate: Date): List<CategorySpending> {
        val categorySpending = mutableListOf<CategorySpending>()
        val db = readableDatabase

        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val query = """
                SELECT $COLUMN_TRANSACTION_CATEGORY, 
                       SUM($COLUMN_TRANSACTION_AMOUNT) as total 
                FROM $TABLE_TRANSACTIONS 
                WHERE $COLUMN_TRANSACTION_USER_ID = ? 
                AND $COLUMN_TRANSACTION_DATE BETWEEN ? AND ? 
                GROUP BY $COLUMN_TRANSACTION_CATEGORY 
                ORDER BY total DESC
            """.trimIndent()

            val cursor = db.rawQuery(query, arrayOf(
                userId.toString(),
                dateFormat.format(startDate),
                dateFormat.format(endDate)
            ))

            while (cursor.moveToNext()) {
                val category = cursor.getString(0)
                val total = cursor.getDouble(1)
                categorySpending.add(CategorySpending(category, total))
            }

            cursor.close()
            categorySpending
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error getting category spending", e)
            categorySpending
        } finally {
            db.close()
        }
    }

    fun getTodayTransactionsByUserId(userId: Int): ArrayList<Transaction> {
        val transactions = ArrayList<Transaction>()
        val db = readableDatabase

        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = dateFormat.format(Date())

            val cursor = db.rawQuery(
                "SELECT * FROM $TABLE_TRANSACTIONS WHERE $COLUMN_TRANSACTION_USER_ID = ? AND $COLUMN_TRANSACTION_DATE = ? ORDER BY $COLUMN_TRANSACTION_DATE DESC",
                arrayOf(userId.toString(), today)
            )

            if (cursor.moveToFirst()) {
                do {
                    transactions.add(Transaction(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID)),
                        userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_USER_ID)),
                        name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_NAME)),
                        company = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_COMPANY)),
                        category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_CATEGORY)),
                        amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_AMOUNT)),
                        date = dateFormat.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_DATE))) ?: Date(),
                        photo = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_PHOTO))
                    ))
                } while (cursor.moveToNext())
            }
            cursor.close()
            transactions
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error getting today's transactions", e)
            transactions
        } finally {
            db.close()
        }
    }

    fun getTotalIncomeByUserId(userId: Int): Double {
        val db = readableDatabase
        return try {
            val query = """
                SELECT SUM($COLUMN_TRANSACTION_AMOUNT) 
                FROM $TABLE_TRANSACTIONS 
                WHERE $COLUMN_TRANSACTION_USER_ID = ? AND $COLUMN_TRANSACTION_AMOUNT > 0
            """.trimIndent()

            val cursor = db.rawQuery(query, arrayOf(userId.toString()))
            var totalIncome = 0.0

            if (cursor.moveToFirst()) {
                totalIncome = cursor.getDouble(0)
            }

            cursor.close()
            totalIncome
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error getting total income", e)
            0.0
        } finally {
            db.close()
        }
    }

    fun getTotalSavingsByUserId(userId: Int): Double {
        val db = readableDatabase
        return try {
            val query = """
                SELECT SUM($COLUMN_TRANSACTION_AMOUNT) 
                FROM $TABLE_TRANSACTIONS 
                WHERE $COLUMN_TRANSACTION_USER_ID = ? AND $COLUMN_TRANSACTION_CATEGORY = 'Savings'
            """.trimIndent()

            val cursor = db.rawQuery(query, arrayOf(userId.toString()))
            var totalSavings = 0.0

            if (cursor.moveToFirst()) {
                totalSavings = cursor.getDouble(0)
            }

            cursor.close()
            totalSavings
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error getting total savings", e)
            0.0
        } finally {
            db.close()
        }
    }

    // --- NOTIFICATIONS ---
    fun addNotification(notification: Notification): Long {
        val db = writableDatabase
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val values = ContentValues().apply {
                put(COLUMN_NOTIFICATION_USER_ID, notification.userId)
                put(COLUMN_NOTIFICATION_MESSAGE, notification.message)
                put(COLUMN_NOTIFICATION_TYPE, notification.type)
                put(COLUMN_NOTIFICATION_AMOUNT, notification.amount)
                put(COLUMN_NOTIFICATION_DATE, dateFormat.format(notification.date))
            }
            db.insert(TABLE_NOTIFICATIONS, null, values)
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error adding notification", e)
            -1
        } finally {
            db.close()
        }
    }

    fun getNotificationsByUserId(userId: Int): ArrayList<Notification> {
        val notifications = ArrayList<Notification>()
        val db = readableDatabase

        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val cursor = db.rawQuery(
                "SELECT * FROM $TABLE_NOTIFICATIONS WHERE $COLUMN_NOTIFICATION_USER_ID = ? ORDER BY $COLUMN_NOTIFICATION_DATE DESC",
                arrayOf(userId.toString())
            )

            if (cursor.moveToFirst()) {
                do {
                    notifications.add(Notification(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_ID)),
                        userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_USER_ID)),
                        message = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_MESSAGE)),
                        type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_TYPE)),
                        amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_AMOUNT)),
                        date = dateFormat.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_DATE))) ?: Date()
                    ))
                } while (cursor.moveToNext())
            }
            cursor.close()
            notifications
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error getting notifications", e)
            notifications
        } finally {
            db.close()
        }
    }
}