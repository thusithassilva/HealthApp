package com.example.healthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.healthapp.databinding.ItemMoodEntryBinding

class MoodAdapter(
    private val moodEntries: List<MoodEntry>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    class MoodViewHolder(private val binding: ItemMoodEntryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(moodEntry: MoodEntry, onDeleteClick: (Int) -> Unit) {
            binding.tvEmoji.text = moodEntry.emoji
            binding.tvMoodLabel.text = moodEntry.moodLabel
            binding.tvDateTime.text = "${moodEntry.date} at ${moodEntry.time}"

            if (moodEntry.note.isNotEmpty()) {
                binding.tvNote.text = moodEntry.note
                binding.tvNote.visibility = View.VISIBLE
            } else {
                binding.tvNote.visibility = View.GONE
            }

            // Set up delete button
            binding.ivDelete.visibility = View.VISIBLE
            binding.ivDelete.setOnClickListener {
                onDeleteClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val binding = ItemMoodEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(moodEntries[position], onDeleteClick)
    }

    override fun getItemCount(): Int = moodEntries.size
}