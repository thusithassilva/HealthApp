package com.example.healthapp

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.healthapp.databinding.ActivityViewProfileBinding

class ViewProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewProfileBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        setupToolbar()
        loadUserData()
        setupClickListeners()
        setupActivityProgress()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun loadUserData() {
        val userName = sharedPreferences.getString("user_name", "User")
        val userEmail = sharedPreferences.getString("user_email", "user@email.com")
        val userAge = sharedPreferences.getString("user_age", "21")
        val userGender = sharedPreferences.getString("user_gender", "Male")
        val userHeight = sharedPreferences.getString("user_height", "165 cm")
        val userWeight = sharedPreferences.getString("user_weight", "45 kg")

        binding.tvUserName.text = userName
        binding.tvUserEmail.text = userEmail
        binding.tvAgeValue.text = userAge
        binding.tvGenderValue.text = userGender
        binding.tvHeightValue.text = userHeight
        binding.tvWeightValue.text = userWeight
    }

    private fun setupActivityProgress() {
        // Load actual progress data from shared preferences or use defaults
        val habitsProgress = sharedPreferences.getInt("habits_progress", 75)
        val moodProgress = sharedPreferences.getInt("mood_progress", 60)
        val hydrationProgress = sharedPreferences.getInt("hydration_progress", 85)

        binding.progressHabits.progress = habitsProgress
        binding.progressMood.progress = moodProgress
        binding.progressHydration.progress = hydrationProgress

        binding.tvHabitsPercent.text = "$habitsProgress%"
        binding.tvMoodPercent.text = "$moodProgress%"
        binding.tvHydrationPercent.text = "$hydrationProgress%"

        // Calculate days/entries based on progress
        val habitsDays = (habitsProgress * 30 / 100)
        val moodEntries = (moodProgress * 30 / 100)
        val waterGlasses = (hydrationProgress * 50 / 100)

        binding.tvHabitsDays.text = "$habitsDays/30 days"
        binding.tvMoodEntries.text = "$moodEntries/30 entries"
        binding.tvWaterGlasses.text = "$waterGlasses/50 glasses"
    }

    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning from edit profile
        loadUserData()
        setupActivityProgress()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}