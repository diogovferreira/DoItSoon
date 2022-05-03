package com.example.doitsoon.ui.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.doitsoon.databinding.TaskModelBinding
import com.example.doitsoon.ui.list.adapter.listitem.TaskItem

class TaskAdapter(private var listOfTasks: List<TaskItem>) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = TaskModelBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val taskItem : TaskItem =listOfTasks[position]
        holder.bind(taskItem)
    }

    override fun getItemCount(): Int {
        return listOfTasks.size
    }

    class TaskViewHolder(private val binding: TaskModelBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(taskItem: TaskItem) {
            with(binding){
                taskText.text = taskItem.taskName
                priorityBell.isVisible = taskItem.isPriority
            }
        }

    }
}