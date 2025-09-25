package com.example.taskmaster.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmaster.R
import com.example.taskmaster.model.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: TaskAdapter
    private val allTasks = mutableListOf<Task>()
    private var currentFilter: String = ""

    companion object {
        // Claves para los extras del Intent al editar/crear tarea
        const val EXTRA_TASK_ID = "com.example.taskmaster.ui.TASK_ID"
        const val EXTRA_TASK_TITLE = "com.example.taskmaster.ui.TASK_TITLE"
        const val EXTRA_TASK_DESCRIPTION = "com.example.taskmaster.ui.TASK_DESCRIPTION"
        const val EXTRA_TASK_PRIORITY = "com.example.taskmaster.ui.TASK_PRIORITY"
        const val EXTRA_TASK_IMPORTANCE = "com.example.taskmaster.ui.TASK_IMPORTANCE"
        const val EXTRA_TASK_IS_URGENT = "com.example.taskmaster.ui.TASK_IS_URGENT"
        const val EXTRA_TASK_IS_DONE = "com.example.taskmaster.ui.TASK_IS_DONE" // Para preservar el estado
        const val EXTRA_IS_EDIT_MODE = "com.example.taskmaster.ui.IS_EDIT_MODE"
    }

    private val taskActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult

                val taskId = data.getStringExtra(EXTRA_TASK_ID)
                val title = data.getStringExtra(EXTRA_TASK_TITLE) ?: return@registerForActivityResult
                val desc = data.getStringExtra(EXTRA_TASK_DESCRIPTION) ?: ""
                val prio = data.getStringExtra(EXTRA_TASK_PRIORITY) ?: getString(R.string.priority_medium)
                val imp = data.getIntExtra(EXTRA_TASK_IMPORTANCE, 3)
                val urgent = data.getBooleanExtra(EXTRA_TASK_IS_URGENT, false)
                // isDone se pasará desde AddTaskActivity si estamos editando,
                // para nuevas tareas será false por defecto en el constructor de Task.
                val isDone = data.getBooleanExtra(EXTRA_TASK_IS_DONE, false)


                if (taskId != null) { // Modo Edición: Actualizar tarea existente
                    val taskToUpdate = allTasks.find { it.id == taskId }
                    taskToUpdate?.apply {
                        this.title = title
                        this.description = desc
                        this.priority = prio
                        this.importance = imp
                        this.isUrgent = urgent
                        this.isDone = isDone // Actualizar también el estado 'isDone'
                    }
                } else { // Modo Creación: Añadir nueva tarea
                    val newTask = Task(
                        // El ID se genera automáticamente en el constructor de Task
                        title = title,
                        description = desc,
                        priority = prio,
                        importance = imp,
                        isUrgent = urgent,
                        isDone = false // Nuevas tareas por defecto no están hechas
                    )
                    allTasks.add(0, newTask)
                }
                applyFilterAndRefreshList(currentFilter)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerTasks)
        val fabAddTask = findViewById<FloatingActionButton>(R.id.fab_add_task)
        val spinnerFilter = findViewById<Spinner>(R.id.spinnerFilter)

        adapter = TaskAdapter(
            mutableListOf(),
            onTaskCheckedChange = { task, isChecked ->
                handleTaskCheckedChanged(task, isChecked)
            },
            onDeleteClicked = { task ->
                showDeleteConfirmationDialog(task)
            },
            onEditClicked = { task -> // Nuevo callback para editar
                handleEditTaskClicked(task)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fabAddTask.setOnClickListener {
            // Iniciar AddTaskActivity para crear una nueva tarea (sin extras de edición)
            val intent = Intent(this, AddTaskActivity::class.java)
            taskActivityResultLauncher.launch(intent)
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.task_filters,
            android.R.layout.simple_spinner_item
        ).also { arrayAdapter ->
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerFilter.adapter = arrayAdapter
        }

        if (spinnerFilter.adapter.count > 0) {
            currentFilter = spinnerFilter.getItemAtPosition(0).toString()
        } else {
            currentFilter = getString(R.string.filter_all_tasks)
        }

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                currentFilter = parent.getItemAtPosition(position).toString()
                applyFilterAndRefreshList(currentFilter)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        loadSampleTasks()
        applyFilterAndRefreshList(currentFilter)
    }

    private fun handleTaskCheckedChanged(task: Task, isChecked: Boolean) {
        val taskInList = allTasks.find { it.id == task.id }
        taskInList?.isDone = isChecked
        applyFilterAndRefreshList(currentFilter)
    }

    private fun showDeleteConfirmationDialog(task: Task) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_delete_title))
            .setMessage(getString(R.string.confirm_delete_message, task.title))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                deleteTask(task)
            }
            .setNegativeButton(getString(R.string.no), null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun deleteTask(task: Task) {
        val taskToRemove = allTasks.find { it.id == task.id }
        if (taskToRemove != null) {
            allTasks.remove(taskToRemove)
            applyFilterAndRefreshList(currentFilter)
        }
    }

    private fun handleEditTaskClicked(task: Task) {
        val intent = Intent(this, AddTaskActivity::class.java).apply {
            putExtra(EXTRA_IS_EDIT_MODE, true)
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_TASK_TITLE, task.title)
            putExtra(EXTRA_TASK_DESCRIPTION, task.description)
            putExtra(EXTRA_TASK_PRIORITY, task.priority)
            putExtra(EXTRA_TASK_IMPORTANCE, task.importance)
            putExtra(EXTRA_TASK_IS_URGENT, task.isUrgent)
            putExtra(EXTRA_TASK_IS_DONE, task.isDone) // Pasar también el estado actual
        }
        taskActivityResultLauncher.launch(intent)
    }

    private fun loadSampleTasks() {
        if (allTasks.isEmpty()) {
            allTasks.addAll(listOf(
                Task(title = "Comprar leche", description = "En el supermercado de la esquina", priority = "Media", importance = 3, isUrgent = false, isDone = false),
                Task(title = "Llamar a mamá", description = "Recordatorio importante para felicitarla", priority = "Alta", importance = 5, isUrgent = true, isDone = false),
                Task(title = "Terminar informe ASD", description = "Para el viernes por la tarde", priority = "Alta", importance = 4, isUrgent = false, isDone = true)
            ))
        }
    }

    private fun applyFilterAndRefreshList(filter: String) {
        val filteredTasks = when (filter) {
            getString(R.string.filter_pending) -> allTasks.filter { !it.isDone }.toMutableList()
            getString(R.string.filter_completed) -> allTasks.filter { it.isDone }.toMutableList()
            getString(R.string.filter_urgent) -> allTasks.filter { it.isUrgent }.toMutableList()
            getString(R.string.filter_high_priority) -> allTasks.filter { it.priority.equals(getString(R.string.priority_high), ignoreCase = true) }.toMutableList()
            getString(R.string.filter_all_tasks) -> allTasks.toMutableList()
            else -> allTasks.toMutableList()
        }
        adapter.updateTasks(filteredTasks)
    }
}
