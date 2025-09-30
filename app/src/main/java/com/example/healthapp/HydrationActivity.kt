package com.example.healthapp

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.healthapp.databinding.ActivityHydrationBinding
import java.text.SimpleDateFormat
import java.util.*

class HydrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHydrationBinding
    private lateinit var sharedPreferences: SharedPreferences

    private var currentWaterIntake = 0.0
    private val dailyWaterGoal = 8 // 8 glasses per day
    private var reminderEnabled = false
    private var reminderInterval = 60 // minutes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHydrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

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
        currentWaterIntake = sharedPreferences.getFloat("water_intake_$today", 0f).toDouble()

        // Load reminder settings
        reminderEnabled = sharedPreferences.getBoolean("water_reminder_enabled", false)
        reminderInterval = sharedPreferences.getInt("water_reminder_interval", 60)
    }

    private fun setupClickListeners() {
        // Add water buttons
        binding.btnAddGlass.setOnClickListener {
            addWater(1.0)
        }

        binding.btnAddHalfGlass.setOnClickListener {
            addWater(0.5)
        }

        binding.btnRemoveGlass.setOnClickListener {
            removeWater(1.0)
        }

        // Reminder toggle
        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            reminderEnabled = isChecked
            saveReminderSettings()
            if (isChecked) {
                Toast.makeText(this, "Water reminders enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Water reminders disabled", Toast.LENGTH_SHORT).show()
            }
        }

        // Reminder interval buttons
        binding.btn30min.setOnClickListener {
            setReminderInterval(30)
        }

        binding.btn60min.setOnClickListener {
            setReminderInterval(60)
        }

        binding.btn90min.setOnClickListener {
            setReminderInterval(90)
        }

        binding.btn120min.setOnClickListener {
            setReminderInterval(120)
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
                    startActivity(android.content.Intent(this, HabitsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_mood -> {
                    startActivity(android.content.Intent(this, MoodActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_stats -> {
                    startActivity(android.content.Intent(this, StatsActivity::class.java))
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

    private fun addWater(amount: Double) {
        currentWaterIntake += amount

        saveHydrationData()
        updateUI()

        val message = if (amount == 0.5) "Half glass added!" else "Glass of water added!"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        // Check if goal is reached
        if (currentWaterIntake >= dailyWaterGoal) {
            binding.tvCongratulations.visibility = android.view.View.VISIBLE
        }
    }

    private fun removeWater(amount: Double) {
        if (currentWaterIntake > 0) {
            currentWaterIntake = maxOf(0.0, currentWaterIntake - amount)
            saveHydrationData()
            updateUI()
            Toast.makeText(this, "Water removed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setReminderInterval(minutes: Int) {
        reminderInterval = minutes
        saveReminderSettings()
        updateReminderUI()
        Toast.makeText(this, "Reminder set to $minutes minutes", Toast.LENGTH_SHORT).show()
    }

    private fun resetDailyIntake() {
        currentWaterIntake = 0.0
        saveHydrationData()
        updateUI()
        Toast.makeText(this, "Daily intake reset", Toast.LENGTH_SHORT).show()
    }

    private fun updateUI() {
        // Update water intake display - format to show .5 for half glasses
        val displayIntake = if (currentWaterIntake % 1 == 0.0) {
            currentWaterIntake.toInt().toString()
        } else {
            String.format("%.1f", currentWaterIntake)
        }
        binding.tvWaterIntake.text = displayIntake
        binding.tvWaterGoal.text = "/ $dailyWaterGoal glasses"

        // Update progress
        val progress = ((currentWaterIntake * 100) / dailyWaterGoal).toInt()
        binding.progressWater.progress = progress
        binding.tvProgressPercent.text = "$progress%"

        // Update water level visualization
        updateWaterLevelVisualization()

        // Show/hide congratulations message
        binding.tvCongratulations.visibility =
            if (currentWaterIntake >= dailyWaterGoal) android.view.View.VISIBLE
            else android.view.View.GONE

        // Update today's date
        val today = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
        binding.tvTodayDate.text = today
    }

    private fun updateWaterLevelVisualization() {
        val waterLevel = ((currentWaterIntake * 100) / dailyWaterGoal).toInt()

        // Update water level in the glass (visual representation)
        when {
            waterLevel >= 100 -> binding.ivWaterLevel.setImageResource(R.drawable.water_level_100)
            waterLevel >= 75 -> binding.ivWaterLevel.setImageResource(R.drawable.water_level_75)
            waterLevel >= 50 -> binding.ivWaterLevel.setImageResource(R.drawable.water_level_50)
            waterLevel >= 25 -> binding.ivWaterLevel.setImageResource(R.drawable.water_level_25)
            else -> binding.ivWaterLevel.setImageResource(R.drawable.water_level_0)
        }

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
        updateButtonBackground(binding.btn30min, reminderInterval == 30)
        updateButtonBackground(binding.btn60min, reminderInterval == 60)
        updateButtonBackground(binding.btn90min, reminderInterval == 90)
        updateButtonBackground(binding.btn120min, reminderInterval == 120)

        binding.tvReminderStatus.text =
            if (reminderEnabled) "Reminders every $reminderInterval minutes"
            else "Reminders disabled"
    }

    private fun updateButtonBackground(button: com.google.android.material.button.MaterialButton, isSelected: Boolean) {
        if (isSelected) {
            button.setBackgroundColor(getColor(R.color.hydration_progress))
            button.setTextColor(getColor(R.color.white))
        } else {
            button.setBackgroundColor(getColor(R.color.white))
            button.setTextColor(getColor(R.color.dark_text))
            button.strokeColor = getColorStateList(R.color.input_border)
        }
    }

    private fun saveHydrationData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val editor = sharedPreferences.edit()
        editor.putFloat("water_intake_$today", currentWaterIntake.toFloat())
        editor.apply()
    }

    private fun saveReminderSettings() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("water_reminder_enabled", reminderEnabled)
        editor.putInt("water_reminder_interval", reminderInterval)
        editor.apply()

        updateReminderUI()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}