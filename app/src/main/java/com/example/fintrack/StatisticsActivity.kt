package com.example.SmartSpender

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.SmartSpender.adapters.TransactionAdapter
import com.example.SmartSpender.database.DatabaseHelper
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.*
import com.example.SmartSpender.models.Wallet
import com.example.SmartSpender.models.Notification
import com.example.SmartSpender.models.Transaction
import com.example.SmartSpender.models.CategorySpending


class StatisticsActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart
    private lateinit var spinnerYear: Spinner
    private lateinit var rvHistory: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var tabLayout: TabLayout
    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var btnSelectStartDate: Button
    private lateinit var btnSelectEndDate: Button
    private lateinit var btnApplyDateFilter: Button
    private lateinit var tvNoData: TextView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var transactionAdapter: TransactionAdapter

    // Gamification views
    private lateinit var tvXP: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvRank: TextView
    private lateinit var progressXP: LinearProgressIndicator
    private lateinit var tvStreak: TextView
    private lateinit var tvBadges: TextView
    private lateinit var tvSavings: TextView
    private lateinit var layoutRecentAchievement: LinearLayout
    private lateinit var tvRecentAchievement: TextView

    private var userId: Int = 0
    private val startCalendar = Calendar.getInstance()
    private val endCalendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

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
        lineChart = findViewById(R.id.lineChart)
        pieChart = findViewById(R.id.pieChart)
        spinnerYear = findViewById(R.id.spinnerYear)
        rvHistory = findViewById(R.id.rvHistory)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        fabAdd = findViewById(R.id.fabAdd)
        tabLayout = findViewById(R.id.tabLayout)
        tvStartDate = findViewById(R.id.tvStartDate)
        tvEndDate = findViewById(R.id.tvEndDate)
        btnSelectStartDate = findViewById(R.id.btnSelectStartDate)
        btnSelectEndDate = findViewById(R.id.btnSelectEndDate)
        btnApplyDateFilter = findViewById(R.id.btnApplyDateFilter)
        tvNoData = findViewById(R.id.tvNoData)

        // Initialize gamification views
        tvXP = findViewById(R.id.tvXP)
        tvLevel = findViewById(R.id.tvLevel)
        tvRank = findViewById(R.id.tvRank)
        progressXP = findViewById(R.id.progressXP)
        tvStreak = findViewById(R.id.tvStreak)
        tvBadges = findViewById(R.id.tvBadges)
        tvSavings = findViewById(R.id.tvSavings)
        layoutRecentAchievement = findViewById(R.id.layoutRecentAchievement)
        tvRecentAchievement = findViewById(R.id.tvRecentAchievement)

        // Initialize database helper
        dbHelper = DatabaseHelper(this)

        // Set up RecyclerView
        rvHistory.layoutManager = LinearLayoutManager(this)
        transactionAdapter = TransactionAdapter(this, ArrayList())
        rvHistory.adapter = transactionAdapter

        // Set up spinner
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.years,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYear.adapter = adapter
        spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val year = parent.getItemAtPosition(position).toString().toInt()
                    loadChartData(year)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        // Set up bottom navigation
        bottomNavigation.setOnNavigationItemSelectedListener(this)
        bottomNavigation.menu.findItem(R.id.navigation_stats).isChecked = true

        // Set up FAB
        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddSpendingActivity::class.java))
        }

        // Set up tab layout
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                updateChartVisibility(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Set up date pickers
        btnSelectStartDate.setOnClickListener {
            showDatePickerDialog(true)
        }

        btnSelectEndDate.setOnClickListener {
            showDatePickerDialog(false)
        }

        btnApplyDateFilter.setOnClickListener {
            loadCategorySpendingData()
        }

        // Set default dates (current month)
        startCalendar.set(Calendar.DAY_OF_MONTH, 1)
        updateDateTextViews()

        // Load data
        loadData()
        setupCharts()
        loadChartData(Calendar.getInstance().get(Calendar.YEAR))
        loadCategorySpendingData()
        loadGamificationData()

        // Default to pie chart view
        tabLayout.getTabAt(1)?.select()
    }

    private fun updateDateTextViews() {
        tvStartDate.text = dateFormat.format(startCalendar.time)
        tvEndDate.text = dateFormat.format(endCalendar.time)
    }

    private fun showDatePickerDialog(isStartDate: Boolean) {
        val calendar = if (isStartDate) startCalendar else endCalendar
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                updateDateTextViews()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateChartVisibility(tabPosition: Int) {
        when (tabPosition) {
            0 -> { // Monthly spending
                lineChart.visibility = View.VISIBLE
                pieChart.visibility = View.GONE
                spinnerYear.visibility = View.VISIBLE
                findViewById<View>(R.id.dateFilterLayout).visibility = View.GONE
                loadChartData(Calendar.getInstance().get(Calendar.YEAR))
            }
            1 -> { // Category breakdown
                lineChart.visibility = View.GONE
                pieChart.visibility = View.VISIBLE
                spinnerYear.visibility = View.GONE
                findViewById<View>(R.id.dateFilterLayout).visibility = View.VISIBLE
                loadCategorySpendingData()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
        loadChartData(Calendar.getInstance().get(Calendar.YEAR))
        loadCategorySpendingData()
        loadGamificationData()
    }

    private fun loadData() {
        // Load today's transactions
        val todayTransactions = dbHelper.getTodayTransactionsByUserId(userId)
        transactionAdapter.updateTransactions(todayTransactions)
    }

    private fun setupCharts() {
        // Setup Line Chart
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = true
        lineChart.legend.textSize = 12f
        lineChart.setDrawGridBackground(false)
        lineChart.setDrawBorders(false)
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.animateX(1500)

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.textSize = 12f
        xAxis.textColor = ContextCompat.getColor(this, R.color.colorText)

        val leftAxis = lineChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.axisMinimum = 0f
        leftAxis.textSize = 12f
        leftAxis.textColor = ContextCompat.getColor(this, R.color.colorText)

        val rightAxis = lineChart.axisRight
        rightAxis.isEnabled = false

        // Setup Pie Chart
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
        pieChart.dragDecelerationFrictionCoef = 0.95f
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(ContextCompat.getColor(this, R.color.colorCard))
        pieChart.setTransparentCircleColor(ContextCompat.getColor(this, R.color.colorCard))
        pieChart.setTransparentCircleAlpha(110)
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f
        pieChart.setDrawCenterText(true)
        pieChart.rotationAngle = 0f
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true
        pieChart.animateY(1400)
        pieChart.legend.isEnabled = true
        pieChart.legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
        pieChart.legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
        pieChart.legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
        pieChart.legend.setDrawInside(false)
        pieChart.legend.textSize = 12f
        pieChart.legend.textColor = ContextCompat.getColor(this, R.color.colorText)
        pieChart.setEntryLabelColor(ContextCompat.getColor(this, R.color.colorText))
        pieChart.setEntryLabelTextSize(12f)
    }

    private fun loadChartData(year: Int) {
        val monthlyData = dbHelper.getMonthlySpendingByYear(userId, year)
        val entries = ArrayList<Entry>()
        val months = ArrayList<String>()

        for (i in 0 until 12) {
            val month = i + 1
            val spending = monthlyData[month] ?: 0.0
            entries.add(Entry(i.toFloat(), Math.abs(spending).toFloat()))
            months.add(getMonthName(month))
        }

        val dataSet = LineDataSet(entries, "Monthly Spending (R)")
        dataSet.color = ContextCompat.getColor(this, R.color.colorPrimary)
        dataSet.setCircleColor(ContextCompat.getColor(this, R.color.colorPrimary))
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.setDrawCircleHole(false)
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = ContextCompat.getColor(this, R.color.colorText)
        dataSet.setDrawFilled(true)
        dataSet.fillColor = ContextCompat.getColor(this, R.color.colorPrimary)
        dataSet.fillAlpha = 30
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.cubicIntensity = 0.2f

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(months)

        lineChart.invalidate()
    }

    private fun loadCategorySpendingData() {
        val startDate = startCalendar.time
        val endDate = endCalendar.time

        if (startDate.after(endDate)) {
            Toast.makeText(this, "Start date cannot be after end date", Toast.LENGTH_SHORT).show()
            return
        }

        val categorySpending = dbHelper.getCategorySpendingByPeriod(userId, startDate, endDate)

        if (categorySpending.isEmpty()) {
            pieChart.visibility = View.GONE
            tvNoData.visibility = View.VISIBLE
            return
        }

        pieChart.visibility = View.VISIBLE
        tvNoData.visibility = View.GONE

        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        for (category in categorySpending) {
            entries.add(PieEntry(Math.abs(category.amount).toFloat(), category.category))

            // Assign colors based on category
            val color = when (category.category.lowercase(Locale.ROOT)) {
                "food" -> ContextCompat.getColor(this, R.color.colorExpense)
                "entertainment" -> ContextCompat.getColor(this, R.color.colorIncome)
                "groceries" -> Color.parseColor("#9C27B0")
                "movies" -> Color.parseColor("#2196F3")
                "transport" -> Color.parseColor("#3D82F7")
                "savings" -> Color.parseColor("#E91E63")
                "utilities" -> Color.parseColor("#607D8B")
                else -> ColorTemplate.MATERIAL_COLORS[categorySpending.indexOf(category) % ColorTemplate.MATERIAL_COLORS.size]
            }
            colors.add(color)
        }

        val dataSet = PieDataSet(entries, "Categories")
        dataSet.colors = colors
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        dataSet.valueLinePart1OffsetPercentage = 80f
        dataSet.valueLinePart1Length = 0.2f
        dataSet.valueLinePart2Length = 0.4f
        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSet.valueTextColor = ContextCompat.getColor(this, R.color.colorText)

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))
        data.setValueTextSize(11f)
        data.setValueTextColor(ContextCompat.getColor(this, R.color.colorText))

        pieChart.data = data
        pieChart.centerText = "Category\nBreakdown"
        pieChart.setCenterTextColor(ContextCompat.getColor(this, R.color.colorText))
        pieChart.invalidate()

        // Update transactions list with filtered data
        val transactions = dbHelper.getTransactionsByPeriod(userId, startDate, endDate)
        transactionAdapter.updateTransactions(transactions)
    }

    private fun loadGamificationData() {
        // Get gamification data from shared preferences
        val sharedPref = getSharedPreferences("SmartSpenderPrefs", MODE_PRIVATE)

        // XP and Level System
        val xp = sharedPref.getInt("userXP", 0)
        val level = calculateLevel(xp)
        val xpForCurrentLevel = getXPForLevel(level)
        val xpForNextLevel = getXPForLevel(level + 1)
        val xpProgress = if (xpForNextLevel > xpForCurrentLevel) {
            ((xp - xpForCurrentLevel).toFloat() / (xpForNextLevel - xpForCurrentLevel).toFloat()) * 100
        } else {
            100f
        }

        // Streak System
        val streak = sharedPref.getInt("userStreak", 0)

        // Badges System
        val badgesCount = sharedPref.getInt("userBadgesCount", 0)

        // Savings Progress
        val savingsProgress = calculateSavingsProgress()

        // Rank System
        val rank = getRankForLevel(level)

        // Recent Achievement
        val recentAchievement = getRecentAchievement()

        // Update UI with gamification data
        tvXP.text = "$xp XP"
        tvLevel.text = "Level $level"
        tvRank.text = rank
        progressXP.progress = xpProgress.toInt()
        tvStreak.text = streak.toString()
        tvBadges.text = badgesCount.toString()
        tvSavings.text = "${savingsProgress}%"

        // Show recent achievement if available
        if (recentAchievement.isNotEmpty()) {
            layoutRecentAchievement.visibility = View.VISIBLE
            tvRecentAchievement.text = recentAchievement
        } else {
            layoutRecentAchievement.visibility = View.GONE
        }
    }

    private fun calculateLevel(xp: Int): Int {
        return when {
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
    }

    private fun getXPForLevel(level: Int): Int {
        return when (level) {
            1 -> 0
            2 -> 50
            3 -> 150
            4 -> 300
            5 -> 500
            6 -> 750
            7 -> 1000
            8 -> 1500
            9 -> 2500
            10 -> 5000
            else -> 0
        }
    }

    private fun getRankForLevel(level: Int): String {
        return when (level) {
            in 1..2 -> "Novice"
            in 3..4 -> "Apprentice"
            in 5..6 -> "Pro"
            in 7..8 -> "Expert"
            in 9..10 -> "Master"
            else -> "Beginner"
        }
    }

    private fun calculateSavingsProgress(): Int {
        // Calculate savings progress based on user's savings goals and actual savings
        val totalIncome = dbHelper.getTotalIncomeByUserId(userId)
        val totalSavings = dbHelper.getTotalSavingsByUserId(userId)

        return if (totalIncome > 0) {
            ((totalSavings / totalIncome) * 100).toInt()
        } else {
            0
        }
    }

    private fun getRecentAchievement(): String {
        val sharedPref = getSharedPreferences("SmartSpenderPrefs", MODE_PRIVATE)
        return sharedPref.getString("recentAchievement", "") ?: ""
    }

    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "Jan"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Apr"
            5 -> "May"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Aug"
            9 -> "Sep"
            10 -> "Oct"
            11 -> "Nov"
            12 -> "Dec"
            else -> ""
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_home -> {
                startActivity(Intent(this, MainActivity::class.java))
                return true
            }
            R.id.navigation_stats -> {
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