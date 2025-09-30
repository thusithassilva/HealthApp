package com.example.healthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HabitsAdapter(
    private val habits: List<HabitItem>,
    private val onHabitChecked: (HabitItem, Boolean) -> Unit,
    private val onHabitLongPress: (HabitItem) -> Unit
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cbHabit: CheckBox = itemView.findViewById(R.id.cbHabit)
        val tvHabitName: TextView = itemView.findViewById(R.id.tvHabitName)
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

        // Strike through text when completed
        if (habit.isCompleted) {
            holder.tvHabitName.paintFlags = holder.tvHabitName.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvHabitName.setTextColor(holder.itemView.context.getColor(android.R.color.darker_gray))
        } else {
            holder.tvHabitName.paintFlags = holder.tvHabitName.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvHabitName.setTextColor(holder.itemView.context.getColor(android.R.color.black))
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