package com.example.healthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class HabitsAdapter(
    private val habits: List<HabitItem>,
    private val onHabitChecked: (HabitItem, Boolean) -> Unit,
    private val onHabitLongPress: (HabitItem) -> Unit
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cbHabit: CheckBox = itemView.findViewById(R.id.cbHabit)
        val tvHabitName: TextView = itemView.findViewById(R.id.tvHabitName)
        val tvCreatedTime: TextView = itemView.findViewById(R.id.tvCreatedTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]

        holder.tvHabitName.text = habit.name
        holder.cbHabit.isChecked = habit.isCompleted

        // Format created date and time
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        val createdTime = dateFormat.format(Date(habit.createdAt))
        holder.tvCreatedTime.text = "Created: $createdTime"

        // Strike through text when completed
        if (habit.isCompleted) {
            holder.tvHabitName.paintFlags = holder.tvHabitName.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvHabitName.setTextColor(holder.itemView.context.getColor(R.color.gray))
            holder.tvCreatedTime.setTextColor(holder.itemView.context.getColor(R.color.light_gray))
        } else {
            holder.tvHabitName.paintFlags = holder.tvHabitName.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvHabitName.setTextColor(holder.itemView.context.getColor(R.color.dark_text))
            holder.tvCreatedTime.setTextColor(holder.itemView.context.getColor(R.color.gray))
        }

        holder.cbHabit.setOnCheckedChangeListener { _, isChecked ->
            onHabitChecked(habit, isChecked)
        }

        holder.itemView.setOnLongClickListener {
            onHabitLongPress(habit)
            true
        }
    }

    override fun getItemCount(): Int = habits.size
}