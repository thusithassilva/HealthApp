package com.example.healthapp

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.healthapp.databinding.ActivityCyclingBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class CyclingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCyclingBinding
    private lateinit var sharedPreferences: SharedPreferences

    private var isCycling = false
    private var cyclingTimer: CountDownTimer? = null
    private var cyclingStartTime: Long = 0
    private var totalCyclingTime = 0L
    private var currentSpeed = 0.0
    private var totalDistance = 0.0
    private var caloriesBurned = 0
    private var currentSessionId = ""

    // Cycling metrics
    private val averageSpeed = 15.0 // km/h
    private val caloriesPerMinute = 8 // Average calories burned per minute

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCyclingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        setupToolbar()
        loadCyclingStats()
        setupClickListeners()
        setupBottomNavigation()
        updateUI()
        startSpeedSimulation()
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
        binding.btnStartCycling.setOnClickListener {
            if (!isCycling) {
                startCyclingSession()
            } else {
                pauseCyclingSession()
            }
        }

        binding.btnStopCycling.setOnClickListener {
            stopCyclingSession()
        }

        binding.btnResetStats.setOnClickListener {
            resetCyclingStats()
        }

        binding.btnShareRide.setOnClickListener {
            shareRideDetails()
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
                    startActivity(android.content.Intent(this, HydrationActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun startCyclingSession() {
        isCycling = true
        cyclingStartTime = System.currentTimeMillis()
        currentSessionId = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()).format(Date())

        binding.btnStartCycling.text = "⏸️ Pause Cycling"
        binding.btnStartCycling.setBackgroundColor(getColor(R.color.pause_color))
        binding.btnStopCycling.isEnabled = true

        startCyclingTimer()
        animateBike()

        Toast.makeText(this, "🚴 Cycling session started!", Toast.LENGTH_SHORT).show()
    }

    private fun pauseCyclingSession() {
        isCycling = false
        cyclingTimer?.cancel()

        binding.btnStartCycling.text = "▶️ Resume Cycling"
        binding.btnStartCycling.setBackgroundColor(getColor(R.color.resume_color))
        stopBikeAnimation()

        Toast.makeText(this, "Cycling paused", Toast.LENGTH_SHORT).show()
    }

    private fun stopCyclingSession() {
        isCycling = false
        cyclingTimer?.cancel()

        val sessionTime = System.currentTimeMillis() - cyclingStartTime
        totalCyclingTime += sessionTime

        saveCyclingSession(sessionTime)
        updateLifetimeStats()

        binding.btnStartCycling.text = "🚴 Start Cycling"
        binding.btnStartCycling.setBackgroundColor(getColor(R.color.start_color))
        binding.btnStopCycling.isEnabled = false

        stopBikeAnimation()
        resetCurrentSession()

        Toast.makeText(this, "✅ Cycling session completed!", Toast.LENGTH_LONG).show()
    }

    private fun startCyclingTimer() {
        cyclingTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateCurrentSessionStats()
            }

            override fun onFinish() {}
        }.start()
    }

    private fun updateCurrentSessionStats() {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - cyclingStartTime
        val minutes = elapsedTime / 60000

        // Update distance based on average speed
        totalDistance = (averageSpeed * minutes / 60.0)
        caloriesBurned = (minutes * caloriesPerMinute).toInt()

        updateUI()
    }

    private fun updateUI() {
        // Current session stats
        val currentTime = System.currentTimeMillis() - cyclingStartTime
        val minutes = (currentTime / 60000).toInt()
        val seconds = ((currentTime % 60000) / 1000).toInt()

        binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
        binding.tvCurrentDistance.text = "%.1f km".format(totalDistance)
        binding.tvCurrentSpeed.text = "%.1f km/h".format(currentSpeed)
        binding.tvCaloriesBurned.text = "$caloriesBurned cal"

        // Update progress
        val distanceProgress = (totalDistance * 100 / 10).toInt() // 10km goal
        binding.progressDistance.progress = minOf(distanceProgress, 100)

        val calorieProgress = (caloriesBurned * 100 / 200) // 200 cal goal
        binding.progressCalories.progress = minOf(calorieProgress, 100)

        // Update stats cards
        updateStatsCards()
    }

    private fun updateStatsCards() {
        // Speed indicator
        when {
            currentSpeed > 20 -> binding.tvSpeedIndicator.text = "🚀 Fast Pace"
            currentSpeed > 15 -> binding.tvSpeedIndicator.text = "💪 Good Pace"
            currentSpeed > 10 -> binding.tvSpeedIndicator.text = "😊 Steady Pace"
            else -> binding.tvSpeedIndicator.text = "🚲 Warm Up"
        }

        // Distance achievement
        when {
            totalDistance >= 10 -> binding.tvDistanceAchievement.text = "🎉 10K Champion!"
            totalDistance >= 5 -> binding.tvDistanceAchievement.text = "⭐ Halfway There!"
            totalDistance >= 2 -> binding.tvDistanceAchievement.text = "🔥 Great Start!"
            else -> binding.tvDistanceAchievement.text = "🚴 Let's Go!"
        }
    }

    private fun startSpeedSimulation() {
        val handler = Handler(Looper.getMainLooper())

        val speedRunnable = object : Runnable {
            override fun run() {
                if (isCycling) {
                    // Simulate realistic speed variations
                    currentSpeed = averageSpeed + (Math.random() * 6 - 3) // ±3 km/h variation
                    updateUI()
                }
                handler.postDelayed(this, 3000) // Update every 3 seconds
            }
        }
        handler.postDelayed(speedRunnable, 3000)
    }

    private fun animateBike() {
        binding.ivBike.animate().cancel()
        binding.ivBike.rotation = 0f

        // Continuous pedaling animation
        binding.ivBike.animate()
            .rotationBy(360f)
            .setDuration(1000)
            .withEndAction {
                if (isCycling) {
                    animateBike()
                }
            }
            .start()

        // Road movement animation
        binding.ivRoad.animate().cancel()
        binding.ivRoad.translationX = 0f

        binding.ivRoad.animate()
            .translationXBy(-100f)
            .setDuration(500)
            .withEndAction {
                if (isCycling) {
                    binding.ivRoad.translationX = 0f
                    animateBike()
                }
            }
            .start()
    }

    private fun stopBikeAnimation() {
        binding.ivBike.animate().cancel()
        binding.ivRoad.animate().cancel()
    }

    private fun saveCyclingSession(sessionTime: Long) {
        val sessionsJson = sharedPreferences.getString("cycling_sessions", "[]")
        val jsonArray = org.json.JSONArray(sessionsJson)

        val sessionData = org.json.JSONObject().apply {
            put("id", currentSessionId)
            put("date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
            put("time", SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
            put("duration", sessionTime)
            put("distance", totalDistance)
            put("calories", caloriesBurned)
            put("averageSpeed", averageSpeed)
        }

        jsonArray.put(sessionData)

        val editor = sharedPreferences.edit()
        editor.putString("cycling_sessions", jsonArray.toString())
        editor.apply()
    }

    private fun loadCyclingStats() {
        val sessionsJson = sharedPreferences.getString("cycling_sessions", "[]")

        try {
            val jsonArray = org.json.JSONArray(sessionsJson)
            var totalSessions = 0
            var totalDuration = 0L
            var totalDistance = 0.0
            var totalCalories = 0

            for (i in 0 until jsonArray.length()) {
                val session = jsonArray.getJSONObject(i)
                totalSessions++
                totalDuration += session.getLong("duration")
                totalDistance += session.getDouble("distance")
                totalCalories += session.getInt("calories")
            }

            binding.tvTotalSessions.text = totalSessions.toString()
            binding.tvTotalDistance.text = "%.1f km".format(totalDistance)
            binding.tvTotalCalories.text = "$totalCalories cal"

            val totalHours = (totalDuration / 3600000.0)
            binding.tvTotalTime.text = "%.1f hours".format(totalHours)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateLifetimeStats() {
        loadCyclingStats()
    }

    private fun resetCurrentSession() {
        totalDistance = 0.0
        caloriesBurned = 0
        currentSpeed = 0.0
        updateUI()
    }

    private fun resetCyclingStats() {
        val editor = sharedPreferences.edit()
        editor.remove("cycling_sessions")
        editor.apply()

        loadCyclingStats()
        resetCurrentSession()

        Toast.makeText(this, "Cycling stats reset", Toast.LENGTH_SHORT).show()
    }

    private fun shareRideDetails() {
        val shareMessage = """
            🚴 My Cycling Achievement!
            ⏱️ Time: ${binding.tvTimer.text}
            📏 Distance: ${binding.tvCurrentDistance.text}
            🔥 Calories: ${binding.tvCaloriesBurned.text}
            💪 Speed: ${binding.tvCurrentSpeed.text}
            
            Tracked with Wellness Tracker App!
        """.trimIndent()

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
        }

        startActivity(Intent.createChooser(intent, "Share your ride"))
    }

    override fun onDestroy() {
        super.onDestroy()
        cyclingTimer?.cancel()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}