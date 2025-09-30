package com.example.healthapp

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthapp.databinding.ActivityMoodBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MoodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoodBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var moodAdapter: MoodAdapter
    private val moodEntries = mutableListOf<MoodEntry>()

    // Emoji options for mood selection
    private val moodEmojis = listOf("😊", "😄", "😍", "😐", "😔", "😢", "😠", "😴", "🤒", "🌟")
    private val moodLabels = listOf("Happy", "Excited", "Loved", "Neutral", "Sad", "Crying", "Angry", "Tired", "Sick", "Great")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        setupToolbar()
        setupMoodList()
        setupEmojiSelection()
        setupClickListeners()
        setupBottomNavigation()
        loadMoodEntriesFromPreferences()
        updateMoodStats()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupMoodList() {
        binding.rvMoodEntries.layoutManager = LinearLayoutManager(this)
        moodAdapter = MoodAdapter(moodEntries)
        binding.rvMoodEntries.adapter = moodAdapter
    }

    private fun setupEmojiSelection() {
        // Setup emoji grid
        val emojiGrid = binding.emojiGrid
        emojiGrid.removeAllViews()

        moodEmojis.forEachIndexed { index, emoji ->
            val emojiButton = android.widget.Button(this).apply {
                text = emoji
                textSize = 20f
                setOnClickListener {
                    selectMood(emoji, moodLabels[index])
                }
                setBackgroundResource(R.drawable.emoji_button_bg)
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    0,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    setMargins(8, 8, 8, 8)
                }
            }
            emojiGrid.addView(emojiButton)
        }
    }

    private fun setupClickListeners() {
        binding.btnAddMoodNote.setOnClickListener {
            addMoodEntryWithNote()
        }

        binding.fabAddQuickMood.setOnClickListener {
            // Quick mood without note
            if (binding.etMoodNote.text.toString().trim().isNotEmpty()) {
                addMoodEntryWithNote()
            } else {
                Toast.makeText(this, "Please select a mood first", Toast.LENGTH_SHORT).show()
            }
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
                    // Already on mood page
                    true
                }
                R.id.nav_stats -> {
                    Toast.makeText(this, "Opening Statistics", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_hydration -> {
                    Toast.makeText(this, "Opening Hydration", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        // Set mood as selected
        binding.bottomNavigationView.selectedItemId = R.id.nav_mood
    }

    private var selectedEmoji: String = ""
    private var selectedMoodLabel: String = ""

    private fun selectMood(emoji: String, label: String) {
        selectedEmoji = emoji
        selectedMoodLabel = label
        binding.tvSelectedMood.text = "$emoji $label"
        binding.tvSelectedMood.visibility = android.view.View.VISIBLE
        binding.btnAddMoodNote.isEnabled = true
    }

    private fun addMoodEntryWithNote() {
        if (selectedEmoji.isEmpty()) {
            Toast.makeText(this, "Please select a mood first", Toast.LENGTH_SHORT).show()
            return
        }

        val note = binding.etMoodNote.text.toString().trim()
        val currentTime = System.currentTimeMillis()

        val moodEntry = MoodEntry(
            id = System.currentTimeMillis().toInt(),
            emoji = selectedEmoji,
            moodLabel = selectedMoodLabel,
            note = note,
            timestamp = currentTime,
            date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(currentTime)),
            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(currentTime))
        )

        moodEntries.add(0, moodEntry) // Add to beginning for newest first
        moodAdapter.notifyItemInserted(0)
        saveMoodEntriesToPreferences()
        updateMoodStats()

        // Reset form
        binding.etMoodNote.setText("")
        binding.tvSelectedMood.visibility = android.view.View.GONE
        selectedEmoji = ""
        selectedMoodLabel = ""
        binding.btnAddMoodNote.isEnabled = false

        Toast.makeText(this, "Mood recorded!", Toast.LENGTH_SHORT).show()
    }

    private fun updateMoodStats() {
        val totalEntries = moodEntries.size
        binding.tvTotalEntries.text = "$totalEntries entries"

        if (totalEntries > 0) {
            // Calculate mood distribution
            val moodCount = moodEntries.groupingBy { it.moodLabel }.eachCount()
            val mostFrequentMood = moodCount.maxByOrNull { it.value }

            mostFrequentMood?.let {
                binding.tvMostFrequentMood.text = "${it.key} (${it.value} times)"
            }

            // Show latest mood
            val latestMood = moodEntries.firstOrNull()
            latestMood?.let {
                binding.tvLatestMood.text = "${it.emoji} ${it.moodLabel}"
            }

            binding.moodStatsSection.visibility = android.view.View.VISIBLE
        } else {
            binding.moodStatsSection.visibility = android.view.View.GONE
        }
    }

    private fun saveMoodEntriesToPreferences() {
        val jsonArray = JSONArray()
        moodEntries.forEach { entry ->
            val jsonObject = JSONObject().apply {
                put("id", entry.id)
                put("emoji", entry.emoji)
                put("moodLabel", entry.moodLabel)
                put("note", entry.note)
                put("timestamp", entry.timestamp)
                put("date", entry.date)
                put("time", entry.time)
            }
            jsonArray.put(jsonObject)
        }

        val editor = sharedPreferences.edit()
        editor.putString("user_mood_entries", jsonArray.toString())
        editor.apply()
    }

    private fun loadMoodEntriesFromPreferences() {
        val moodJson = sharedPreferences.getString("user_mood_entries", "[]")

        try {
            val jsonArray = JSONArray(moodJson)

            moodEntries.clear()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val moodEntry = MoodEntry(
                    id = jsonObject.getInt("id"),
                    emoji = jsonObject.getString("emoji"),
                    moodLabel = jsonObject.getString("moodLabel"),
                    note = jsonObject.getString("note"),
                    timestamp = jsonObject.getLong("timestamp"),
                    date = jsonObject.getString("date"),
                    time = jsonObject.getString("time")
                )
                moodEntries.add(moodEntry)
            }

            // Sort by timestamp (newest first)
            moodEntries.sortByDescending { it.timestamp }
            moodAdapter.notifyDataSetChanged()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}

data class MoodEntry(
    val id: Int,
    val emoji: String,
    val moodLabel: String,
    val note: String,
    val timestamp: Long,
    val date: String,
    val time: String
)