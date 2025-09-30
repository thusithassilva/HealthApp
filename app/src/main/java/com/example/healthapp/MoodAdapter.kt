package com.example.healthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MoodAdapter(private val moodEntries: List<MoodEntry>) :
    RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEmoji: TextView = itemView.findViewById(R.id.tvEmoji)
        val tvMoodLabel: TextView = itemView.findViewById(R.id.tvMoodLabel)
        val tvNote: TextView = itemView.findViewById(R.id.tvNote)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_entry, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val moodEntry = moodEntries[position]

        holder.tvEmoji.text = moodEntry.emoji
        holder.tvMoodLabel.text = moodEntry.moodLabel
        holder.tvDateTime.text = "${moodEntry.date} at ${moodEntry.time}"

        if (moodEntry.note.isNotEmpty()) {
            holder.tvNote.text = moodEntry.note
            holder.tvNote.visibility = View.VISIBLE
        } else {
            holder.tvNote.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = moodEntries.size
}