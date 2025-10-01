package com.example.healthapp

import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.healthapp.databinding.ActivityCyclingBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class CyclingActivity : AppCompatActivity(), SensorEventListener, LocationListener {

    private lateinit var binding: ActivityCyclingBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager

    private var isCycling = false
    private var cyclingTimer: CountDownTimer? = null
    private var cyclingStartTime: Long = 0
    private var pausedTime: Long = 0
    private var totalCyclingTime = 0L
    private var currentSpeed = 0.0
    private var totalDistance = 0.0
    private var caloriesBurned = 0
    private var currentSessionId = ""

    // Sensor data
    private var stepCount = 0
    private var lastStepTime = 0L
    private var lastLocation: Location? = null

    // Cycling metrics
    private val caloriesPerMinute = 8 // Average calories burned per minute

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCyclingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        setupToolbar()
        setupSensors()
        setupLocation()
        loadCyclingStats()
        setupClickListeners()
        setupBottomNavigation()
        updateUI()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupSensors() {
        // Step counter sensor
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // Accelerometer for movement detection
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun setupLocation() {
        try {
            // Check location permissions
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1f, this)
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Location permission required for accurate tracking", Toast.LENGTH_LONG).show()
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

        if (cyclingStartTime == 0L) {
            // New session
            cyclingStartTime = System.currentTimeMillis()
            currentSessionId = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()).format(Date())
        } else {
            // Resuming session - adjust start time based on paused duration
            val pausedDuration = System.currentTimeMillis() - pausedTime
            cyclingStartTime += pausedDuration
        }

        binding.btnStartCycling.text = "⏸️ Pause Cycling"
        binding.btnStartCycling.setBackgroundColor(getColor(R.color.pause_color))
        binding.btnStopCycling.isEnabled = true

        startCyclingTimer()
        animateBike()

        Toast.makeText(this, "🚴 Cycling session started!", Toast.LENGTH_SHORT).show()
    }

    private fun pauseCyclingSession() {
        isCycling = false
        pausedTime = System.currentTimeMillis()
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

        // Reset for new session
        cyclingStartTime = 0
        pausedTime = 0
        totalDistance = 0.0
        caloriesBurned = 0
        currentSpeed = 0.0
        lastLocation = null

        stopBikeAnimation()
        updateUI()

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

        // Calculate calories based on time and movement
        caloriesBurned = (minutes * caloriesPerMinute).toInt()

        updateUI()
    }

    private fun updateUI() {
        // Current session stats
        val currentTime = if (isCycling) {
            System.currentTimeMillis() - cyclingStartTime
        } else if (cyclingStartTime > 0) {
            pausedTime - cyclingStartTime
        } else {
            0
        }

        val minutes = (currentTime / 60000).toInt()
        val seconds = ((currentTime % 60000) / 1000).toInt()

        binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
        binding.tvCurrentDistance.text = "%.2f km".format(totalDistance)
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
            currentSpeed > 0 -> binding.tvSpeedIndicator.text = "🚲 Warm Up"
            else -> binding.tvSpeedIndicator.text = "⏸️ Paused"
        }

        // Distance achievement
        when {
            totalDistance >= 10 -> binding.tvDistanceAchievement.text = "🎉 10K Champion!"
            totalDistance >= 5 -> binding.tvDistanceAchievement.text = "⭐ Halfway There!"
            totalDistance >= 2 -> binding.tvDistanceAchievement.text = "🔥 Great Start!"
            totalDistance > 0 -> binding.tvDistanceAchievement.text = "🚴 Let's Go!"
            else -> binding.tvDistanceAchievement.text = "📍 Ready to Start!"
        }
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

        // Road movement animation - speed based on actual speed
        val animationDuration = maxOf(500L, (2000 / (currentSpeed + 1)).toLong())

        binding.ivRoad.animate().cancel()
        binding.ivRoad.translationX = 0f

        binding.ivRoad.animate()
            .translationXBy(-100f)
            .setDuration(animationDuration)
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

    // SensorEventListener methods
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (event.sensor.type) {
                Sensor.TYPE_STEP_COUNTER -> {
                    // Step counting for cadence estimation
                    if (isCycling) {
                        stepCount = event.values[0].toInt()
                        // Estimate speed based on step cadence
                        val currentTime = System.currentTimeMillis()
                        if (lastStepTime > 0) {
                            val timeDiff = currentTime - lastStepTime
                            if (timeDiff > 0) {
                                val stepsPerMinute = (stepCount * 60000 / timeDiff).toFloat()
                                // Convert steps to approximate cycling speed
                                currentSpeed = (stepsPerMinute * 0.1).toDouble()
                            }
                        }
                        lastStepTime = currentTime
                    }
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    // Movement detection for speed estimation
                    if (isCycling) {
                        val acceleration = Math.sqrt(
                            event.values[0].toDouble() * event.values[0] +
                                    event.values[1].toDouble() * event.values[1] +
                                    event.values[2].toDouble() * event.values[2]
                        )

                        // Simple speed estimation based on movement
                        if (acceleration > 12) {
                            currentSpeed = minOf(currentSpeed + 0.5, 30.0)
                        } else if (acceleration < 8) {
                            currentSpeed = maxOf(currentSpeed - 0.2, 0.0)
                        }
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // LocationListener methods
    override fun onLocationChanged(location: Location) {
        if (isCycling) {
            lastLocation?.let { previousLocation ->
                // Calculate distance between locations
                val distance = location.distanceTo(previousLocation) / 1000.0 // Convert to km
                totalDistance += distance

                // Calculate real speed from GPS
                currentSpeed = location.speed * 3.6 // Convert m/s to km/h
                if (currentSpeed < 0) currentSpeed = 0.0
            }
            lastLocation = location
        }
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

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
            put("averageSpeed", if (sessionTime > 0) totalDistance / (sessionTime / 3600000.0) else 0.0)
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
            binding.tvTotalDistance.text = "%.2f km".format(totalDistance)
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

    private fun resetCyclingStats() {
        val editor = sharedPreferences.edit()
        editor.remove("cycling_sessions")
        editor.apply()

        loadCyclingStats()

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
        sensorManager.unregisterListener(this)

        try {
            locationManager.removeUpdates(this)
        } catch (e: SecurityException) {
            // Ignore
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}