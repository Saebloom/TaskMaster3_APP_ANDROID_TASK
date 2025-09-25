package com.example.taskmaster.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmaster.R
import com.example.taskmaster.model.Task

class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onTaskCheckedChange: (Task, Boolean) -> Unit,
    private val onDeleteClicked: (Task) -> Unit,
    private val onEditClicked: (Task) -> Unit // Nuevo: para manejar el clic en editar
) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.item_task_title)
        val doneCheckBox: CheckBox = itemView.findViewById(R.id.item_task_checkbox)
        val deleteButton: ImageButton = itemView.findViewById(R.id.button_delete_task)
        val editButton: ImageButton = itemView.findViewById(R.id.button_edit_task) // Nuevo
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.titleTextView.text = task.title

        holder.doneCheckBox.setOnCheckedChangeListener(null)
        holder.doneCheckBox.isChecked = task.isDone
        holder.doneCheckBox.setOnCheckedChangeListener { _, isChecked ->
            onTaskCheckedChange(task, isChecked)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClicked(task)
        }

        holder.editButton.setOnClickListener { // Nuevo
            onEditClicked(task) // Notificar a la Activity/ViewModel
        }
    }

    override fun getItemCount(): Int = tasks.size

    fun addTask(task: Task) {
        tasks.add(0, task)
        notifyItemInserted(0)
    }

    fun updateTasks(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }

    fun removeTask(task: Task) {
        val position = tasks.indexOf(task)
        if (position != -1) {
            tasks.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
