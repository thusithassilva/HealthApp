package com.example.healthapp

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.healthapp.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

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

        // Set up goals (you can modify this later)
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
        // Mock data for goals progress
        binding.progressWater.progress = 60
        binding.progressSteps.progress = 40
        binding.progressSleep.progress = 80

        binding.tvWaterPercent.text = "60%"
        binding.tvStepsPercent.text = "40%"
        binding.tvSleepPercent.text = "80%"
    }

    private fun setupClickListeners() {
        // Quick Actions
        binding.cardHabits.setOnClickListener {
            Toast.makeText(this, "Opening Habits", Toast.LENGTH_SHORT).show()
            // Navigate to Habits page
        }

        binding.cardMood.setOnClickListener {
            Toast.makeText(this, "Opening Mood Journal", Toast.LENGTH_SHORT).show()
            // Navigate to Mood page
        }

        binding.cardStats.setOnClickListener {
            Toast.makeText(this, "Opening Statistics", Toast.LENGTH_SHORT).show()
            // Navigate to Stats page
        }

        binding.cardHydration.setOnClickListener {
            Toast.makeText(this, "Opening Hydration", Toast.LENGTH_SHORT).show()
            // Navigate to Hydration page
        }

        // Profile icon click
        binding.ivProfile.setOnClickListener {
            showProfileMenu()
        }

        binding.cardHabits.setOnClickListener {
            startActivity(Intent(this, HabitsActivity::class.java))
        }

        binding.cardMood.setOnClickListener {
            startActivity(Intent(this, MoodActivity::class.java))
        }
        binding.cardStats.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }
        binding.cardHydration.setOnClickListener {
            startActivity(Intent(this, HydrationActivity::class.java))
        }
        // Add to setupClickListeners() in MainActivity
        binding.cardCycling.setOnClickListener {
            startActivity(Intent(this, CyclingActivity::class.java))
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