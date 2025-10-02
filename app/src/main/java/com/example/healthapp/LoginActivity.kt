package com.example.healthapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import com.example.healthapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkIfUserRegistered()
        setupClickListeners()
        setupPasswordVisibility()
    }

    private fun checkIfUserRegistered() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isRegistered = sharedPreferences.getBoolean("is_registered", false)

        if (!isRegistered) {
            // If not registered, go to register page
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        binding.tvForgotPasscode.setOnClickListener {
            Toast.makeText(this, "Please re-register with new passcode", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupPasswordVisibility() {
        // Set initial state to show dots
        binding.etPasscode.transformationMethod = PasswordTransformationMethod.getInstance()

        // Set end icon for password visibility toggle
        binding.tilPasscode.setEndIconOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            binding.etPasscode.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.tilPasscode.endIconDrawable = getDrawable(com.google.android.material.R.drawable.design_ic_visibility)
        } else {
            // Show password
            binding.etPasscode.transformationMethod = null
            binding.tilPasscode.endIconDrawable = getDrawable(com.google.android.material.R.drawable.design_ic_visibility_off)
        }
        isPasswordVisible = !isPasswordVisible

        // Move cursor to the end
        binding.etPasscode.setSelection(binding.etPasscode.text?.length ?: 0)
    }

    private fun loginUser() {
        val enteredPasscode = binding.etPasscode.text.toString().trim()

        if (enteredPasscode.length != 4) {
            binding.etPasscode.error = "Please enter 4-digit passcode"
            return
        }

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val savedPasscode = sharedPreferences.getString("user_passcode", "")

        if (enteredPasscode == savedPasscode) {
            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

            // Mark user as logged in
            val editor = sharedPreferences.edit()
            editor.putBoolean("is_logged_in", true)
            editor.apply()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Invalid passcode. Please try again.", Toast.LENGTH_SHORT).show()
            binding.etPasscode.error = "Invalid passcode"
        }
    }
}