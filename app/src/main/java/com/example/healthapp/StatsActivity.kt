package com.example.healthapp

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.healthapp.databinding.ActivityStatsBinding
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        setupToolbar()
        setupClickListeners()
        setupBottomNavigation()
        loadAndDisplayStats()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupClickListeners() {
        binding.btnRefreshStats.setOnClickListener {
            loadAndDisplayStats()
            Toast.makeText(this, "Stats refreshed!", Toast.LENGTH_SHORT).show()
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
                    // Already on stats page
                    true
                }
                R.id.nav_hydration -> {
                    startActivity(android.content.Intent(this, HydrationActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        // Set stats as selected
        binding.bottomNavigationView.selectedItemId = R.id.nav_stats
    }

    private fun loadAndDisplayStats() {
        loadHabitsStats()
        loadMoodStats()
        loadCyclingStats()
        loadWeeklyProgress()
        loadOverallWellnessScore()
    }

    private fun loadHabitsStats() {
        val habitsJson = sharedPreferences.getString("user_habits", "[]")

        try {
            val jsonArray = JSONArray(habitsJson)
            val totalHabits = jsonArray.length()

            var completedHabits = 0
            var totalCompletionRate = 0.0

            for (i in 0 until jsonArray.length()) {
                val habit = jsonArray.getJSONObject(i)
                if (habit.getBoolean("isCompleted")) {
                    completedHabits++
                }
            }

            val completionRate = if (totalHabits > 0) {
                (completedHabits * 100.0) / totalHabits
            } else {
                0.0
            }

            // Update UI
            binding.tvTotalHabits.text = totalHabits.toString()
            binding.tvCompletedHabits.text = completedHabits.toString()
            binding.tvHabitsCompletionRate.text = "%.1f%%".format(completionRate)
            binding.progressHabitsCompletion.progress = completionRate.toInt()

            // Calculate streak (simplified - you can enhance this)
            val currentStreak = calculateHabitStreak()
            binding.tvCurrentStreak.text = "$currentStreak days"

        } catch (e: Exception) {
            e.printStackTrace()
            binding.tvTotalHabits.text = "0"
            binding.tvCompletedHabits.text = "0"
            binding.tvHabitsCompletionRate.text = "0%"
            binding.progressHabitsCompletion.progress = 0
            binding.tvCurrentStreak.text = "0 days"
        }
    }

    private fun loadMoodStats() {
        val moodJson = sharedPreferences.getString("user_mood_entries", "[]")

        try {
            val jsonArray = JSONArray(moodJson)
            val totalMoodEntries = jsonArray.length()

            // Mood distribution
            val moodCount = mutableMapOf<String, Int>()
            var latestMood = "No entries"
            var latestMoodDate = ""

            for (i in 0 until jsonArray.length()) {
                val moodEntry = jsonArray.getJSONObject(i)
                val moodLabel = moodEntry.getString("moodLabel")
                moodCount[moodLabel] = moodCount.getOrDefault(moodLabel, 0) + 1

                // Get latest mood
                if (i == 0) {
                    latestMood = moodEntry.getString("emoji") + " " + moodLabel
                    latestMoodDate = moodEntry.getString("date")
                }
            }

            // Find most frequent mood
            val mostFrequentMood = moodCount.maxByOrNull { it.value }

            // Update UI
            binding.tvTotalMoodEntries.text = totalMoodEntries.toString()
            binding.tvLatestMood.text = latestMood
            binding.tvLatestMoodDate.text = latestMoodDate

            if (mostFrequentMood != null) {
                binding.tvMostFrequentMood.text = "${mostFrequentMood.key} (${mostFrequentMood.value} times)"
            } else {
                binding.tvMostFrequentMood.text = "No data"
            }

            // Calculate average mood (simplified)
            val avgMoodScore = calculateAverageMoodScore()
            binding.tvAvgMoodScore.text = "%.1f/10".format(avgMoodScore)
            binding.progressAvgMood.progress = (avgMoodScore * 10).toInt()

        } catch (e: Exception) {
            e.printStackTrace()
            binding.tvTotalMoodEntries.text = "0"
            binding.tvLatestMood.text = "No entries"
            binding.tvLatestMoodDate.text = ""
            binding.tvMostFrequentMood.text = "No data"
            binding.tvAvgMoodScore.text = "0.0/10"
            binding.progressAvgMood.progress = 0
        }
    }

    private fun loadCyclingStats() {
        val cyclingJson = sharedPreferences.getString("cycling_sessions", "[]")

        try {
            val jsonArray = JSONArray(cyclingJson)
            val totalSessions = jsonArray.length()

            var totalCyclingDistance = 0.0
            var totalCyclingTime = 0L
            var totalCyclingCalories = 0
            var longestDistance = 0.0
            var fastestSpeed = 0.0

            for (i in 0 until jsonArray.length()) {
                val session = jsonArray.getJSONObject(i)
                totalCyclingDistance += session.getDouble("distance")
                totalCyclingTime += session.getLong("duration")
                totalCyclingCalories += session.getInt("calories")

                // Check for longest distance
                val sessionDistance = session.getDouble("distance")
                if (sessionDistance > longestDistance) {
                    longestDistance = sessionDistance
                }

                // Check for fastest speed
                val sessionSpeed = session.getDouble("averageSpeed")
                if (sessionSpeed > fastestSpeed) {
                    fastestSpeed = sessionSpeed
                }
            }

            // Update UI
            binding.tvCyclingSessions.text = totalSessions.toString()
            binding.tvTotalCyclingDistance.text = "%.1f km".format(totalCyclingDistance)
            binding.tvTotalCyclingCalories.text = "$totalCyclingCalories cal"

            val totalHours = totalCyclingTime / 3600000.0
            binding.tvTotalCyclingTime.text = "%.1f hours".format(totalHours)

            binding.tvLongestRide.text = "%.1f km".format(longestDistance)
            binding.tvFastestSpeed.text = "%.1f km/h".format(fastestSpeed)

        } catch (e: Exception) {
            e.printStackTrace()
            binding.tvCyclingSessions.text = "0"
            binding.tvTotalCyclingDistance.text = "0.0 km"
            binding.tvTotalCyclingCalories.text = "0 cal"
            binding.tvTotalCyclingTime.text = "0.0 hours"
            binding.tvLongestRide.text = "0.0 km"
            binding.tvFastestSpeed.text = "0.0 km/h"
        }
    }

    private fun loadWeeklyProgress() {
        // Calculate weekly completion rate
        val habitsJson = sharedPreferences.getString("user_habits", "[]")

        try {
            val jsonArray = JSONArray(habitsJson)
            val calendar = Calendar.getInstance()
            val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)

            var weeklyCompleted = 0
            var weeklyTotal = 0

            for (i in 0 until jsonArray.length()) {
                val habit = jsonArray.getJSONObject(i)
                val createdAt = habit.getLong("createdAt")
                val habitCalendar = Calendar.getInstance().apply {
                    timeInMillis = createdAt
                }

                if (habitCalendar.get(Calendar.WEEK_OF_YEAR) == currentWeek) {
                    weeklyTotal++
                    if (habit.getBoolean("isCompleted")) {
                        weeklyCompleted++
                    }
                }
            }

            val weeklyProgress = if (weeklyTotal > 0) {
                (weeklyCompleted * 100.0) / weeklyTotal
            } else {
                0.0
            }

            binding.tvWeeklyProgress.text = "%.1f%%".format(weeklyProgress)
            binding.progressWeekly.progress = weeklyProgress.toInt()

        } catch (e: Exception) {
            e.printStackTrace()
            binding.tvWeeklyProgress.text = "0%"
            binding.progressWeekly.progress = 0
        }
    }

    private fun loadOverallWellnessScore() {
        // Calculate overall wellness score based on habits, mood, and cycling
        val habitsJson = sharedPreferences.getString("user_habits", "[]")
        val moodJson = sharedPreferences.getString("user_mood_entries", "[]")
        val cyclingJson = sharedPreferences.getString("cycling_sessions", "[]")

        try {
            val habitsArray = JSONArray(habitsJson)
            val moodArray = JSONArray(moodJson)
            val cyclingArray = JSONArray(cyclingJson)

            // Habits component (40%)
            val totalHabits = habitsArray.length()
            var completedHabits = 0
            for (i in 0 until habitsArray.length()) {
                val habit = habitsArray.getJSONObject(i)
                if (habit.getBoolean("isCompleted")) {
                    completedHabits++
                }
            }
            val habitsScore = if (totalHabits > 0) (completedHabits * 40.0) / totalHabits else 0.0

            // Mood component (40%)
            val moodScore = calculateAverageMoodScore() * 4 // Convert 0-10 to 0-40

            // Cycling component (20%) - based on recent activity
            val cyclingScore = calculateCyclingScore(cyclingArray)

            val overallScore = habitsScore + moodScore + cyclingScore

            binding.tvWellnessScore.text = "%.1f/100".format(overallScore)
            binding.progressWellnessScore.progress = overallScore.toInt()

            // Set wellness level
            val wellnessLevel = when {
                overallScore >= 80 -> "Excellent 🌟"
                overallScore >= 60 -> "Good 👍"
                overallScore >= 40 -> "Fair 🙂"
                else -> "Needs Improvement 📈"
            }
            binding.tvWellnessLevel.text = wellnessLevel

        } catch (e: Exception) {
            e.printStackTrace()
            binding.tvWellnessScore.text = "0.0/100"
            binding.progressWellnessScore.progress = 0
            binding.tvWellnessLevel.text = "No data"
        }
    }

    private fun calculateCyclingScore(cyclingArray: JSONArray): Double {
        if (cyclingArray.length() == 0) return 0.0

        val calendar = Calendar.getInstance()
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        var weeklySessions = 0

        for (i in 0 until cyclingArray.length()) {
            val session = cyclingArray.getJSONObject(i)
            val sessionDate = session.getString("date")
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sessionCalendar = Calendar.getInstance().apply {
                time = dateFormat.parse(sessionDate) ?: Date()
            }

            if (sessionCalendar.get(Calendar.WEEK_OF_YEAR) == currentWeek) {
                weeklySessions++
            }
        }

        // Score based on weekly cycling sessions (max 20 points)
        return minOf(weeklySessions * 5.0, 20.0)
    }

    private fun calculateHabitStreak(): Int {
        // Simplified streak calculation - in real app, track daily completions
        val habitsJson = sharedPreferences.getString("user_habits", "[]")

        try {
            val jsonArray = JSONArray(habitsJson)
            val completedToday = jsonArray.length() > 0 &&
                    jsonArray.getJSONObject(0).getBoolean("isCompleted")

            return if (completedToday) {
                // Return a random streak for demo (1-7 days)
                Random().nextInt(7) + 1
            } else {
                0
            }
        } catch (e: Exception) {
            return 0
        }
    }

    private fun calculateAverageMoodScore(): Double {
        val moodJson = sharedPreferences.getString("user_mood_entries", "[]")

        try {
            val jsonArray = JSONArray(moodJson)
            if (jsonArray.length() == 0) return 0.0

            var totalScore = 0.0
            val moodScores = mapOf(
                "Great" to 10.0, "Excited" to 9.0, "Happy" to 8.0, "Loved" to 9.5,
                "Neutral" to 5.0, "Sad" to 3.0, "Crying" to 2.0, "Angry" to 2.5,
                "Tired" to 4.0, "Sick" to 1.0
            )

            for (i in 0 until jsonArray.length()) {
                val moodEntry = jsonArray.getJSONObject(i)
                val moodLabel = moodEntry.getString("moodLabel")
                totalScore += moodScores.getOrDefault(moodLabel, 5.0)
            }

            return totalScore / jsonArray.length()
        } catch (e: Exception) {
            return 0.0
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}