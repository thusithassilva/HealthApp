package com.example.healthapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.healthapp.databinding.ActivityRegisterBinding
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val passcode = binding.etPasscode.text.toString().trim()
        val confirmPasscode = binding.etConfirmPasscode.text.toString().trim()

        if (validateInputs(name, email, passcode, confirmPasscode)) {
            saveUserDetails(name, email, passcode)
            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validateInputs(name: String, email: String, passcode: String, confirmPasscode: String): Boolean {
        if (name.isEmpty()) {
            binding.etName.error = "Please enter your name"
            return false
        }

        if (email.isEmpty()) {
            binding.etEmail.error = "Please enter your email"
            return false
        }

        if (!isValidEmail(email)) {
            binding.etEmail.error = "Please enter a valid email address"
            return false
        }

        if (passcode.length != 4) {
            binding.etPasscode.error = "Passcode must be 4 digits"
            return false
        }

        if (passcode != confirmPasscode) {
            binding.etConfirmPasscode.error = "Passcodes do not match"
            return false
        }

        return true
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        val pattern = Pattern.compile(emailRegex)
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

    private fun saveUserDetails(name: String, email: String, passcode: String) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("user_name", name)
        editor.putString("user_email", email)
        editor.putString("user_passcode", passcode)
        editor.putBoolean("is_registered", true)

        editor.apply()
    }
}