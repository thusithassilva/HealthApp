package com.example.healthapp

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.view.MenuItem
import com.example.healthapp.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        setupToolbar()
        loadUserData()
        setupClickListeners()
        setupBottomNavigation()

        // Check if we should show passcode section
        if (intent.getBooleanExtra("SHOW_PASSCODE_SECTION", false)) {
            binding.passcodeSection.visibility = android.view.View.VISIBLE
        }
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

        binding.etName.setText(userName)
        binding.etEmail.setText(userEmail)
        binding.etCurrentPasscode.setText("")
        binding.etNewPasscode.setText("")
        binding.etConfirmPasscode.setText("")
    }

    private fun setupClickListeners() {
        binding.btnSaveProfile.setOnClickListener {
            saveProfileChanges()
        }

        binding.btnUpdatePasscode.setOnClickListener {
            changePasscode()
        }

        binding.btnShowPasscodeSection.setOnClickListener {
            binding.passcodeSection.visibility = android.view.View.VISIBLE
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_habits -> { Toast.makeText(this, "Habits", Toast.LENGTH_SHORT).show(); true }
                R.id.nav_mood -> { Toast.makeText(this, "Mood Journal", Toast.LENGTH_SHORT).show(); true }
                R.id.nav_stats -> { Toast.makeText(this, "Statistics", Toast.LENGTH_SHORT).show(); true }
                R.id.nav_hydration -> { Toast.makeText(this, "Hydration", Toast.LENGTH_SHORT).show(); true }
                else -> false
            }
        }

    }

    private fun saveProfileChanges() {
        val newName = binding.etName.text.toString().trim()
        val newEmail = binding.etEmail.text.toString().trim()

        if (newName.isEmpty()) {
            binding.etName.error = "Please enter your name"
            return
        }

        if (newEmail.isEmpty()) {
            binding.etEmail.error = "Please enter your email"
            return
        }

        val editor = sharedPreferences.edit()
        editor.putString("user_name", newName)
        editor.putString("user_email", newEmail)
        editor.apply()

        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun changePasscode() {
        val currentPasscode = binding.etCurrentPasscode.text.toString().trim()
        val newPasscode = binding.etNewPasscode.text.toString().trim()
        val confirmPasscode = binding.etConfirmPasscode.text.toString().trim()

        val savedPasscode = sharedPreferences.getString("user_passcode", "")

        if (currentPasscode.isEmpty()) {
            binding.etCurrentPasscode.error = "Please enter current passcode"
            return
        }

        if (currentPasscode != savedPasscode) {
            binding.etCurrentPasscode.error = "Current passcode is incorrect"
            return
        }

        if (newPasscode.length != 4) {
            binding.etNewPasscode.error = "Passcode must be 4 digits"
            return
        }

        if (newPasscode != confirmPasscode) {
            binding.etConfirmPasscode.error = "Passcodes do not match"
            return
        }

        val editor = sharedPreferences.edit()
        editor.putString("user_passcode", newPasscode)
        editor.apply()

        Toast.makeText(this, "Passcode changed successfully!", Toast.LENGTH_SHORT).show()

        // Clear passcode fields
        binding.etCurrentPasscode.setText("")
        binding.etNewPasscode.setText("")
        binding.etConfirmPasscode.setText("")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}