package com.example.healthapp

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.healthapp.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        setupToolbar()
        loadUserData()
        setupClickListeners()
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
        val userPasscode = sharedPreferences.getString("user_passcode", "0000")

        binding.etName.setText(userName)
        binding.etEmail.setText(userEmail)
        binding.etCurrentPasscode.setText("")
        binding.etNewPasscode.setText("")
        binding.etConfirmPasscode.setText("")

        binding.tvUserName.text = userName
        binding.tvUserEmail.text = userEmail
    }

    private fun setupClickListeners() {
        binding.btnSaveChanges.setOnClickListener {
            saveProfileChanges()
        }

        binding.btnChangePasscode.setOnClickListener {
            changePasscode()
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

        // Email validation
        if (!isValidEmail(newEmail)) {
            binding.etEmail.error = "Please enter a valid email address"
            return
        }

        val editor = sharedPreferences.edit()
        editor.putString("user_name", newName)
        editor.putString("user_email", newEmail)
        editor.apply()

        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()

        // Update displayed name and email
        binding.tvUserName.text = newName
        binding.tvUserEmail.text = newEmail
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

    // Email validation function
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}