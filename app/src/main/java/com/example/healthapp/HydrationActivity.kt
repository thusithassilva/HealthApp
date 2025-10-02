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
        // Add water buttons
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

        val message = if (amount == 0.5f) "Half glass added!" else "Glass of water added!"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

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
            binding.tvCongratulations.visibility = android.view.View.GONE
            Toast.makeText(this, "Water removed", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No water to remove!", Toast.LENGTH_SHORT).show()
        }
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
        val intent = Intent(this, WaterReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val intervalMillis = reminderInterval * 1000L // Convert seconds to milliseconds

        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + intervalMillis,
            intervalMillis,
            pendingIntent
        )
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
        pendingIntent.cancel()
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

    override fun onDestroy() {
        super.onDestroy()
        // Clean up reminders when activity is destroyed
        if (!reminderEnabled) {
            cancelWaterReminder()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}