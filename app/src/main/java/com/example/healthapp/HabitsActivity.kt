package com.example.healthapp

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthapp.databinding.ActivityHabitsBinding
import org.json.JSONArray
import org.json.JSONObject

class HabitsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHabitsBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var habitsAdapter: HabitsAdapter
    private val habitsList = mutableListOf<HabitItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHabitsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        setupToolbar()
        setupHabitsList()
        setupClickListeners()
        setupBottomNavigation()
        loadHabitsFromPreferences()
        updateProgress()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupHabitsList() {
        // Set up RecyclerView with LinearLayoutManager
        binding.rvHabits.layoutManager = LinearLayoutManager(this)

        habitsAdapter = HabitsAdapter(
            habitsList,
            onHabitChecked = { habit, isChecked ->
                updateHabitCompletion(habit.id, isChecked)
                updateProgress()
            },
            onHabitLongPress = { habit ->
                deleteHabit(habit.id)
            }
        )
        binding.rvHabits.adapter = habitsAdapter
    }

    private fun setupClickListeners() {
        // FAB click listener

        // Add button in the card click listener
        binding.btnAddHabit.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_habits -> {
                    // Already on habits page
                    true
                }
                R.id.nav_mood -> {
                    startActivity(android.content.Intent(this, MoodActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_stats -> {
                    startActivity(Intent(this, StatsActivity::class.java))
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

        // Set habits as selected
        binding.bottomNavigationView.selectedItemId = R.id.nav_habits
    }

    private fun showAddHabitDialog() {
        val habitName = binding.etNewHabit.text.toString().trim()

        if (habitName.isEmpty()) {
            binding.etNewHabit.error = "Please enter a habit name"
            return
        }

        val newHabit = HabitItem(
            id = System.currentTimeMillis().toInt(),
            name = habitName,
            isCompleted = false,
            createdAt = System.currentTimeMillis()
        )

        habitsList.add(0, newHabit)
        habitsAdapter.notifyItemInserted(0)
        binding.rvHabits.scrollToPosition(0)
        saveHabitsToPreferences()
        updateProgress()

        binding.etNewHabit.setText("")
        Toast.makeText(this, "Habit added successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun deleteHabit(habitId: Int) {
        val index = habitsList.indexOfFirst { it.id == habitId }
        if (index != -1) {
            habitsList.removeAt(index)
            habitsAdapter.notifyItemRemoved(index)
            saveHabitsToPreferences()
            updateProgress()
            Toast.makeText(this, "Habit deleted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateHabitCompletion(habitId: Int, isCompleted: Boolean) {
        val habit = habitsList.find { it.id == habitId }
        habit?.isCompleted = isCompleted
        saveHabitsToPreferences()
    }

    private fun updateProgress() {
        val totalHabits = habitsList.size
        val completedHabits = habitsList.count { it.isCompleted }

        val progress = if (totalHabits > 0) {
            (completedHabits * 100) / totalHabits
        } else {
            0
        }

        binding.progressDaily.progress = progress
        binding.tvProgress.text = "$progress%"
        binding.tvHabitsCount.text = "$completedHabits/$totalHabits completed"

        if (progress == 100 && totalHabits > 0) {
            binding.tvCongratulations.visibility = android.view.View.VISIBLE
        } else {
            binding.tvCongratulations.visibility = android.view.View.GONE
        }
    }

    private fun saveHabitsToPreferences() {
        val jsonArray = JSONArray()
        habitsList.forEach { habit ->
            val jsonObject = JSONObject().apply {
                put("id", habit.id)
                put("name", habit.name)
                put("isCompleted", habit.isCompleted)
                put("createdAt", habit.createdAt)
            }
            jsonArray.put(jsonObject)
        }

        val editor = sharedPreferences.edit()
        editor.putString("user_habits", jsonArray.toString())
        editor.apply()
    }

    private fun loadHabitsFromPreferences() {
        val hasKey = sharedPreferences.contains("user_habits")
        val habitsJson = sharedPreferences.getString("user_habits", if (hasKey) "[]" else null)

        try {
            val jsonArray = JSONArray(habitsJson)

            habitsList.clear()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val habit = HabitItem(
                    id = jsonObject.getInt("id"),
                    name = jsonObject.getString("name"),
                    isCompleted = jsonObject.getBoolean("isCompleted"),
                    createdAt = jsonObject.getLong("createdAt")
                )
                habitsList.add(habit)
            }

            habitsAdapter.notifyDataSetChanged()

            // Only add sample habits once if key not present at all
            if (!hasKey && habitsList.isEmpty()) {
                addSampleHabits()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // If there's an error loading habits and no prior key, add sample habits
            if (!hasKey) {
                addSampleHabits()
            }
        }
    }

    private fun addSampleHabits() {
        val sampleHabits = listOf(
            HabitItem(1, "Drink 8 glasses of water", false, System.currentTimeMillis()),
            HabitItem(2, "30 minutes exercise", false, System.currentTimeMillis()),
            HabitItem(3, "Meditate for 10 minutes", false, System.currentTimeMillis()),
            HabitItem(4, "Read a book", false, System.currentTimeMillis()),
            HabitItem(5, "Get 8 hours sleep", false, System.currentTimeMillis())
        )

        habitsList.clear()
        habitsList.addAll(sampleHabits)
        habitsAdapter.notifyDataSetChanged()
        saveHabitsToPreferences()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}

data class HabitItem(
    val id: Int,
    val name: String,
    var isCompleted: Boolean,
    val createdAt: Long
)