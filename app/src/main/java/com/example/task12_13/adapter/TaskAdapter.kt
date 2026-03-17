package com.example.task12_13.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.task12_13.databinding.ItemTaskBinding
import com.example.task12_13.model.Task

/**
 * RecyclerView Adapter for displaying a list of Tasks using DiffUtil for efficient updates
 */
class TaskAdapter(
    private val onDeleteClick: (Task) -> Unit,
    private val onCheckChanged: (Task, Boolean) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding, onDeleteClick, onCheckChanged)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder for Task items
     */
    class TaskViewHolder(
        private val binding: ItemTaskBinding,
        private val onDeleteClick: (Task) -> Unit,
        private val onCheckChanged: (Task, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.apply {
                tvTitle.text = task.title
                tvDescription.text = task.description
                cbCompleted.isChecked = task.completed

                // Set text appearance based on completion status
                tvTitle.alpha = if (task.completed) 0.5f else 1.0f
                tvDescription.alpha = if (task.completed) 0.5f else 1.0f

                // Set up delete button click listener
                btnDelete.setOnClickListener {
                    onDeleteClick(task)
                }

                // Set up checkbox change listener
                cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                    onCheckChanged(task, isChecked)
                    tvTitle.alpha = if (isChecked) 0.5f else 1.0f
                    tvDescription.alpha = if (isChecked) 0.5f else 1.0f
                }

                // Set click listener on the entire item
                root.setOnClickListener {
                    cbCompleted.toggle()
                }
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates
     */
    private class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}
