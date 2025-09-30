package com.example.healthapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.example.healthapp.databinding.ActivityOnboardingBinding


class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var onboardingAdapter: OnboardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupOnboardingPages()
        setupIndicators()
        setupClicks()
    }

    private fun setupOnboardingPages() {
        val onboardingPages = listOf(
            OnboardingPage(
                "Track Your Habits",
                "Monitor daily wellness activities and build healthy routines",
                R.drawable.ic_habits
            ),
            OnboardingPage(
                "Log Your Mood",
                "Record your daily mood with emojis and track your emotional journey",
                R.drawable.ic_mood
            ),
            OnboardingPage(
                "Stay Hydrated",
                "Get reminders to drink water and maintain your hydration goals",
                R.drawable.ic_water
            )
        )

        onboardingAdapter = OnboardingAdapter(onboardingPages)
        binding.viewPagerOnboarding.adapter = onboardingAdapter
    }

    private fun setupIndicators() {
        binding.viewPagerOnboarding.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicators(position)

                // Show/hide buttons based on position
                if (position == onboardingAdapter.itemCount - 1) {
                    binding.btnNext.text = "Continue to Login"
                } else {
                    binding.btnNext.text = "Next"
                }
            }
        })
    }

    private fun updateIndicators(position: Int) {
        val indicators = arrayOf(binding.indicator1, binding.indicator2, binding.indicator3)

        indicators.forEachIndexed { index, indicator ->
            if (index == position) {
                indicator.setBackgroundResource(R.drawable.indicator_active)
            } else {
                indicator.setBackgroundResource(R.drawable.indicator_inactive)
            }
        }
    }

    private fun setupClicks() {
        binding.btnNext.setOnClickListener {
            if (binding.viewPagerOnboarding.currentItem < onboardingAdapter.itemCount - 1) {
                binding.viewPagerOnboarding.currentItem += 1
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        binding.btnSkip.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}

data class OnboardingPage(
    val title: String,
    val description: String,
    val imageRes: Int
)