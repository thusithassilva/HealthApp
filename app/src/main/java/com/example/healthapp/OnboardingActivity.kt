package com.example.healthapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.healthapp.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var sharedPreferences: SharedPreferences

    private val onboardingItems = listOf(
        OnboardingItem(
            imageRes = R.drawable.ob1,
            title = "Track Your Health Journey",
            description = "Monitor your daily habits, mood, hydration, and cycling activities all in one beautiful interface."
        ),
        OnboardingItem(
            imageRes = R.drawable.ob2,
            title = "Build Healthy Habits",
            description = "Create meaningful routines and track your progress with intuitive tools designed for success."
        ),
        OnboardingItem(
            imageRes = R.drawable.ob3,
            title = "Achieve Wellness Goals",
            description = "Set personal targets and celebrate your achievements on the path to better health."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        setupViewPager()
        setupClickListeners()
        updateIndicators(0)
    }

    private fun setupViewPager() {
        val adapter = OnboardingAdapter(onboardingItems)
        binding.viewPagerOnboarding.adapter = adapter

        binding.viewPagerOnboarding.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicators(position)
                updateButtonText(position)
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnSkip.setOnClickListener {
            completeOnboarding()
        }

        binding.btnNext.setOnClickListener {
            val currentItem = binding.viewPagerOnboarding.currentItem
            if (currentItem < onboardingItems.size - 1) {
                binding.viewPagerOnboarding.currentItem = currentItem + 1
            } else {
                completeOnboarding()
            }
        }
    }

    private fun updateIndicators(position: Int) {
        val indicators = listOf(binding.indicator1, binding.indicator2, binding.indicator3)

        indicators.forEachIndexed { index, indicator ->
            if (index == position) {
                if (index == 0) {
                    indicator.setBackgroundResource(R.drawable.indicator_active_rect)
                } else {
                    indicator.setBackgroundResource(R.drawable.indicator_active)
                }
            } else {
                indicator.setBackgroundResource(R.drawable.indicator_inactive)
            }
        }
    }

    private fun updateButtonText(position: Int) {
        binding.btnNext.text = if (position == onboardingItems.size - 1) "Get Started" else "Next"
    }

    private fun completeOnboarding() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("onboarding_completed", true)
        editor.apply()

        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    // Move the OnboardingItem data class inside the activity
    data class OnboardingItem(
        val imageRes: Int,
        val title: String,
        val description: String
    )

    // Move the adapter class inside the activity
    class OnboardingAdapter(private val items: List<OnboardingItem>) :
        RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_onboarding, parent, false)
            return OnboardingViewHolder(view)
        }

        override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val imageView: ImageView = itemView.findViewById(R.id.ivOnboarding)
            private val titleView: TextView = itemView.findViewById(R.id.tvTitle)
            private val descriptionView: TextView = itemView.findViewById(R.id.tvDescription)

            fun bind(item: OnboardingItem) {
                imageView.setImageResource(item.imageRes)
                titleView.text = item.title
                descriptionView.text = item.description
            }
        }
    }
}