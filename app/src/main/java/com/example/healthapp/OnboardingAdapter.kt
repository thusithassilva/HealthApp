package com.example.healthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OnboardingAdapter(private val items: List<OnboardingActivity.OnboardingItem>) :
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

        fun bind(item: OnboardingActivity.OnboardingItem) {
            imageView.setImageResource(item.imageRes)
            titleView.text = item.title
            descriptionView.text = item.description
        }
    }
}