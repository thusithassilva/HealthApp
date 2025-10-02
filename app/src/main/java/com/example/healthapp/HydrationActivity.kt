package com.example.healthapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import android.widget.LinearLayout
import com.example.healthapp.databinding.ActivityHydrationBinding
import java.text.SimpleDateFormat
import java.util.*

class HydrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHydrationBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var alarmManager: AlarmManager

    private var currentWaterIntake = 0.0f
    private val dailyWaterGoal = 8 // 8 glasses per day
    private var reminderEnabled = false
    private var reminderInterval = 5 // seconds for testing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHydrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        setupToolbar()
        loadHydrationData()
        setupClickListeners()
        setupBottomNavigation()
        updateUI()
        updateReminderUI()
        updateWeeklyChart()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun loadHydrationData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Load today's water intake
        currentWaterIntake = sharedPreferences.getFloat("water_intake_$today", 0f)

        // Load reminder settings
        reminderEnabled = sharedPreferences.getBoolean("water_reminder_enabled", false)
        reminderInterval = sharedPreferences.getInt("water_reminder_interval", 5)
    }

    private fun setupClickListeners() {
        // Add water buttons - using CardView click listeners
        binding.btnAddGlass.setOnClickListener {
            addWater(1.0f)
        }

        binding.btnAddHalfGlass.setOnClickListener {
            addWater(0.5f)
        }

        binding.btnRemoveGlass.setOnClickListener {
            removeWater(1.0f)
        }

        // Reminder toggle
        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            reminderEnabled = isChecked
            saveReminderSettings()
            if (isChecked) {
                startWaterReminder()
                Toast.makeText(this, "Water reminders enabled every $reminderInterval seconds", Toast.LENGTH_SHORT).show()
            } else {
                cancelWaterReminder()
                Toast.makeText(this, "Water reminders disabled", Toast.LENGTH_SHORT).show()
            }
        }

        // Reminder interval buttons - Updated to seconds
        binding.btn30min.setOnClickListener {
            setReminderInterval(5) // 5 seconds for testing
        }

        binding.btn60min.setOnClickListener {
            setReminderInterval(10) // 10 seconds for testing
        }

        binding.btn90min.setOnClickListener {
            setReminderInterval(15) // 15 seconds for testing
        }

        // Reset daily intake
        binding.btnResetToday.setOnClickListener {
            resetDailyIntake()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_habits -> {
                    startActivity(Intent(this, HabitsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_mood -> {
                    startActivity(Intent(this, MoodActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_stats -> {
                    startActivity(Intent(this, StatsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_hydration -> {
                    // Already on hydration page
                    true
                }
                else -> false
            }
        }

        // Set hydration as selected
        binding.bottomNavigationView.selectedItemId = R.id.nav_hydration
    }

    private fun addWater(amount: Float) {
        // Check if adding water would exceed the daily goal
        if (currentWaterIntake + amount > dailyWaterGoal) {
            Toast.makeText(this, "Cannot exceed daily goal of $dailyWaterGoal glasses!", Toast.LENGTH_SHORT).show()
            return
        }

        currentWaterIntake += amount
        saveHydrationData()
        updateUI()
        updateWeeklyChart()

        // Removed success toast messages for add water buttons
        // Only show error toast for maximum limit (kept above)

        // Check if goal is reached
        if (currentWaterIntake >= dailyWaterGoal) {
            binding.tvCongratulations.visibility = android.view.View.VISIBLE
            Toast.makeText(this, "🎉 Congratulations! You reached your daily water goal!", Toast.LENGTH_LONG).show()
        }
    }

    private fun removeWater(amount: Float) {
        if (currentWaterIntake > 0) {
            currentWaterIntake = maxOf(0.0f, currentWaterIntake - amount)
            saveHydrationData()
            updateUI()
            updateWeeklyChart()
            binding.tvCongratulations.visibility = android.view.View.GONE
            // Removed toast message for remove water button
        }
        // Removed else condition toast for "No water to remove"
    }

    private fun setReminderInterval(seconds: Int) {
        val wasEnabled = reminderEnabled
        if (wasEnabled) {
            cancelWaterReminder()
        }

        reminderInterval = seconds
        saveReminderSettings()
        updateReminderUI()

        if (wasEnabled) {
            startWaterReminder()
        }

        Toast.makeText(this, "Reminder set to $seconds seconds", Toast.LENGTH_SHORT).show()
    }

    private fun resetDailyIntake() {
        currentWaterIntake = 0.0f
        saveHydrationData()
        updateUI()
        updateWeeklyChart()
        binding.tvCongratulations.visibility = android.view.View.GONE
        Toast.makeText(this, "Daily intake reset", Toast.LENGTH_SHORT).show()
    }

    private fun updateUI() {
        // Update water label
        val displayIntake = if (currentWaterIntake % 1 == 0.0f) {
            currentWaterIntake.toInt().toString()
        } else {
            String.format("%.1f", currentWaterIntake)
        }
        binding.tvWaterLabel.text = "💧 Today's Intake ($displayIntake/$dailyWaterGoal glasses)"

        // Update progress
        val progress = ((currentWaterIntake * 100) / dailyWaterGoal).toInt()
        binding.progressWater.progress = minOf(progress, 100)
        binding.tvProgressPercent.text = "$progress%"

        // Update water level visualization
        updateWaterLevelVisualization()

        // Show/hide congratulations message
        binding.tvCongratulations.visibility =
            if (currentWaterIntake >= dailyWaterGoal) android.view.View.VISIBLE
            else android.view.View.GONE

        // Update today's date
        val today = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
        binding.tvTodayDate.text = today
    }

    private fun updateWaterLevelVisualization() {
        val waterLevel = ((currentWaterIntake * 100) / dailyWaterGoal).toInt()

        // Update hydration status message
        val statusMessage = when {
            waterLevel >= 100 -> "Excellent! You've reached your goal! 🎉"
            waterLevel >= 75 -> "Great job! Almost there! 👍"
            waterLevel >= 50 -> "Good progress! Halfway there! 💪"
            waterLevel >= 25 -> "Keep going! You can do it! 🌟"
            else -> "Let's start hydrating! 💧"
        }
        binding.tvHydrationStatus.text = statusMessage
    }

    private fun updateWeeklyChart() {
        // Get water intake for the past 7 days
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val weeklyData = mutableListOf<Float>()

        // Get data for last 7 days
        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date = dateFormat.format(calendar.time)
            val waterIntake = sharedPreferences.getFloat("water_intake_$date", 0f)
            weeklyData.add(waterIntake)
        }

        // Update chart bars
        updateChartBar(binding.barMon, weeklyData[0])
        updateChartBar(binding.barTue, weeklyData[1])
        updateChartBar(binding.barWed, weeklyData[2])
        updateChartBar(binding.barThu, weeklyData[3])
        updateChartBar(binding.barFri, weeklyData[4])
        updateChartBar(binding.barSat, weeklyData[5])
        updateChartBar(binding.barSun, weeklyData[6])
    }

    private fun updateChartBar(barView: android.view.View, waterIntake: Float) {
        val progress = ((waterIntake * 100) / dailyWaterGoal).toInt()

        // Calculate height based on progress (minimum 10% for visibility)
        val maxHeight = 100 // 100% of available space
        val barHeight = maxOf((progress * maxHeight) / 100, 10)

        val layoutParams = barView.layoutParams
        layoutParams.height = barHeight
        barView.layoutParams = layoutParams

        // Update bar color based on progress
        val barColor = when {
            progress >= 100 -> R.color.hydration_progress
            progress >= 50 -> R.color.hydration_light
            else -> R.color.light_gray
        }
        barView.setBackgroundColor(getColor(barColor))
    }

    private fun updateReminderUI() {
        binding.switchReminder.isChecked = reminderEnabled

        // Update interval button states
        updateButtonBackground(binding.btn30min, reminderInterval == 5)
        updateButtonBackground(binding.btn60min, reminderInterval == 10)
        updateButtonBackground(binding.btn90min, reminderInterval == 15)

        binding.tvReminderStatus.text =
            if (reminderEnabled) "Reminders every $reminderInterval seconds"
            else "Reminders disabled"
    }

    private fun updateButtonBackground(button: com.google.android.material.button.MaterialButton, isSelected: Boolean) {
        if (isSelected) {
            button.setBackgroundColor(getColor(R.color.hydration_progress))
            button.setTextColor(getColor(R.color.white))
            button.strokeColor = getColorStateList(R.color.hydration_progress)
        } else {
            button.setBackgroundColor(getColor(R.color.white))
            button.setTextColor(getColor(R.color.dark_text))
            button.strokeColor = getColorStateList(R.color.input_border)
        }
    }

    private fun startWaterReminder() {
        cancelWaterReminder() // Cancel any existing reminders first

        val intent = Intent(this, WaterReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val intervalMillis = reminderInterval * 1000L // Convert seconds to milliseconds

        // Use setExactAndAllowWhileIdle for precise timing on newer Android versions
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + intervalMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + intervalMillis,
                pendingIntent
            )
        }

        // Schedule the next reminder recursively
        scheduleNextReminder(pendingIntent, intervalMillis)
    }

    private fun scheduleNextReminder(pendingIntent: PendingIntent, intervalMillis: Long) {
        if (!reminderEnabled) return

        val nextTriggerTime = SystemClock.elapsedRealtime() + intervalMillis

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                nextTriggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                nextTriggerTime,
                pendingIntent
            )
        }
    }

    private fun cancelWaterReminder() {
        val intent = Intent(this, WaterReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    private fun saveHydrationData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val editor = sharedPreferences.edit()
        editor.putFloat("water_intake_$today", currentWaterIntake)
        editor.apply()
    }

    private fun saveReminderSettings() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("water_reminder_enabled", reminderEnabled)
        editor.putInt("water_reminder_interval", reminderInterval)
        editor.apply()

        updateReminderUI()
    }

    override fun onResume() {
        super.onResume()
        // Restart reminders if they were enabled
        if (reminderEnabled) {
            startWaterReminder()
        }
        updateWeeklyChart()
    }

    override fun onPause() {
        super.onPause()
        // We don't cancel reminders on pause to keep them running in background
    }

    override fun onDestroy() {
        super.onDestroy()
        // We don't cancel reminders on destroy to keep them running
        // They will be managed by the system
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}