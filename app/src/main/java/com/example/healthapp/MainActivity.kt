package com.example.healthapp

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.healthapp.databinding.ActivityMainBinding
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    // Goal targets
    private val hydrationGoal = 8
    private val habitsGoal = 3
    private val moodGoal = 3
    private val cyclingGoal = 5.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Set toolbar as action bar
        setSupportActionBar(binding.toolbar)

        checkUserLogin()
        setupUI()
        setupClickListeners()
        setupNavigation()
    }

    override fun onResume() {
        super.onResume()
        // Update goals progress when returning to main activity
        updateGoalsProgress()
    }

    private fun checkUserLogin() {
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)
        if (!isLoggedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupUI() {
        // Set greeting based on time
        binding.tvGreeting.text = getGreetingMessage()

        // Set user profile info
        val userName = sharedPreferences.getString("user_name", "User")
        val userEmail = sharedPreferences.getString("user_email", "user@email.com")

        binding.tvUserName.text = userName
        binding.tvUserEmail.text = userEmail

        // Set today's date
        binding.tvDate.text = getCurrentDate()

        // Set up goals with real data
        updateGoalsProgress()
    }

    private fun getGreetingMessage(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        return when {
            hour < 12 -> "Good Morning"
            hour < 18 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        return "$day/$month/$year"
    }

    private fun updateGoalsProgress() {
        // Get real data from SharedPreferences for each activity
        updateHydrationProgress()
        updateHabitsProgress()
        updateMoodProgress()
        updateCyclingProgress()
    }

    private fun updateHydrationProgress() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val waterIntake = sharedPreferences.getFloat("water_intake_$today", 0f)

        val hydrationProgress = ((waterIntake / hydrationGoal) * 100).toInt()
        binding.progressWater.progress = minOf(hydrationProgress, 100)
        binding.tvWaterPercent.text = "$hydrationProgress%"

        // Update label to show actual intake
        binding.tvWaterLabel.text = "🔵 Hydration (${waterIntake.toInt()}/$hydrationGoal glasses)"
    }

    private fun updateHabitsProgress() {
        val habitsJson = sharedPreferences.getString("user_habits", "[]")
        var completedHabitsToday = 0
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        try {
            val jsonArray = JSONArray(habitsJson)

            for (i in 0 until jsonArray.length()) {
                val habit = jsonArray.getJSONObject(i)
                val completionDates = habit.optJSONArray("completionDates") ?: JSONArray()

                for (j in 0 until completionDates.length()) {
                    if (completionDates.getString(j) == today) {
                        completedHabitsToday++
                        break
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateMoodProgress() {
        val moodJson = sharedPreferences.getString("user_mood_entries", "[]")
        var moodEntriesToday = 0
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        try {
            val jsonArray = JSONArray(moodJson)

            for (i in 0 until jsonArray.length()) {
                val moodEntry = jsonArray.getJSONObject(i)
                val moodDate = moodEntry.getString("date")

                // Check if mood entry is from today
                if (moodDate == today) {
                    moodEntriesToday++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val moodProgress = ((moodEntriesToday.toDouble() / moodGoal) * 100).toInt()

        binding.progressMood.progress = minOf(moodProgress, 100)
        binding.tvMoodPercent.text = "$moodProgress%"
        binding.tvMoodLabel.text = "🔵 Mood ($moodEntriesToday/$moodGoal entries)"
    }

    private fun updateCyclingProgress() {
        val cyclingSessionsJson = sharedPreferences.getString("cycling_sessions", "[]")
        var totalDistance = 0.0
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        try {
            val jsonArray = JSONArray(cyclingSessionsJson)

            for (i in 0 until jsonArray.length()) {
                val session = jsonArray.getJSONObject(i)
                val sessionDate = session.getString("date")

                if (sessionDate == today) {
                    totalDistance += session.getDouble("distance")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val cyclingProgress = ((totalDistance / cyclingGoal) * 100).toInt()

        binding.progressCycling.progress = minOf(cyclingProgress, 100)
        binding.tvCyclingPercent.text = "$cyclingProgress%"
        binding.tvCyclingLabel.text = "🔵 Cycling (${"%.1f".format(totalDistance)}/$cyclingGoal km)"
    }

    private fun setupClickListeners() {
        // Quick Actions
        binding.cardHabits.setOnClickListener {
            startActivity(Intent(this, HabitsActivity::class.java))
        }

        binding.cardMood.setOnClickListener {
            startActivity(Intent(this, MoodActivity::class.java))
        }

        binding.cardHydration.setOnClickListener {
            startActivity(Intent(this, HydrationActivity::class.java))
        }

        binding.cardCycling.setOnClickListener {
            startActivity(Intent(this, CyclingActivity::class.java))
        }

        // Profile icon click
        binding.ivProfile.setOnClickListener {
            showProfileMenu()
        }
    }

    private fun setupNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_habits -> {
                    startActivity(Intent(this, HabitsActivity::class.java))
                    true
                }
                R.id.nav_mood -> {
                    startActivity(Intent(this, MoodActivity::class.java))
                    true
                }
                R.id.nav_stats -> {
                    startActivity(Intent(this, StatsActivity::class.java))
                    true
                }
                R.id.nav_hydration -> {
                    startActivity(Intent(this, HydrationActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun showProfileMenu() {
        // Simple profile menu implementation
        val popupMenu = android.widget.PopupMenu(this, binding.ivProfile)
        popupMenu.menu.add(Menu.NONE, 1, Menu.NONE, "View Profile")

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    // Navigate to ViewProfileActivity
                    startActivity(Intent(this, ViewProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                showSettingsMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSettingsMenu() {
        val popupMenu = android.widget.PopupMenu(this, findViewById(R.id.menu_settings))
        popupMenu.menuInflater.inflate(R.menu.settings_menu, popupMenu.menu)

        // Force show icons on right side
        try {
            val field = popupMenu.javaClass.getDeclaredField("mPopup")
            field.isAccessible = true
            val popup = field.get(popupMenu)
            popup.javaClass
                .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(popup, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_update_profile -> {
                    // Navigate to Profile Activity
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.menu_logout -> {
                    showLogoutConfirmation()
                    true
                }
                else -> false
            }
        }

        // Show the popup menu aligned to the right
        popupMenu.gravity = android.view.Gravity.END
        popupMenu.show()
    }

    private fun showLogoutConfirmation() {
        val alertDialog = android.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("OK") { dialog, which ->
                // User clicked OK, proceed with logout
                logout()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                // User clicked Cancel, stay on main page
                dialog.dismiss()
                Toast.makeText(this, "Logout cancelled", Toast.LENGTH_SHORT).show()
            }
            .create()

        alertDialog.show()
    }

    private fun logout() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("is_logged_in", false)
        editor.apply()

        startActivity(Intent(this, LoginActivity::class.java))
        finish()

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }
}