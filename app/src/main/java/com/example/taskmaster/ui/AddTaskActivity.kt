package com.example.taskmaster.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RatingBar
import androidx.appcompat.app.AppCompatActivity
import com.example.taskmaster.R
import com.google.android.material.appbar.MaterialToolbar // Importado
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AddTaskActivity : AppCompatActivity() {

    private var isEditMode = false
    private var currentTaskId: String? = null
    private var currentTaskIsDone: Boolean = false // Para preservar el estado 'isDone' al editar

    private lateinit var etTitle: TextInputEditText
    private lateinit var etDesc: TextInputEditText
    private lateinit var radioGroup: RadioGroup
    private lateinit var rbHigh: RadioButton
    private lateinit var rbMedium: RadioButton
    private lateinit var rbLow: RadioButton
    private lateinit var ratingBarImportance: RatingBar // Nombre de variable cambiado para claridad
    private lateinit var cbUrgent: CheckBox
    private lateinit var btnSave: MaterialButton
    private lateinit var toolbar: MaterialToolbar // Tipo cambiado a MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        toolbar = findViewById(R.id.toolbar_add_task) // Asignar a la variable de clase
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        etTitle = findViewById(R.id.etTitle)
        etDesc = findViewById(R.id.etDescription)
        radioGroup = findViewById(R.id.radioGroup)
        rbHigh = findViewById(R.id.rbAlta)
        rbMedium = findViewById(R.id.rbMedia)
        rbLow = findViewById(R.id.rbBaja)
        ratingBarImportance = findViewById(R.id.ratingImportance) // Asignar a la variable de clase
        cbUrgent = findViewById(R.id.cb_urgent_task)
        btnSave = findViewById(R.id.btnSave)
        val btnCancel = findViewById<MaterialButton>(R.id.btnCancel)

        // Comprobar si estamos en modo edición
        if (intent.hasExtra(MainActivity.EXTRA_IS_EDIT_MODE)) {
            isEditMode = intent.getBooleanExtra(MainActivity.EXTRA_IS_EDIT_MODE, false)
        }

        if (isEditMode) {
            currentTaskId = intent.getStringExtra(MainActivity.EXTRA_TASK_ID)
            currentTaskIsDone = intent.getBooleanExtra(MainActivity.EXTRA_TASK_IS_DONE, false) // Recuperar isDone

            toolbar.title = getString(R.string.edit_task_screen_title)
            btnSave.text = getString(R.string.button_update_task)

            etTitle.setText(intent.getStringExtra(MainActivity.EXTRA_TASK_TITLE))
            etDesc.setText(intent.getStringExtra(MainActivity.EXTRA_TASK_DESCRIPTION))
            ratingBarImportance.rating = intent.getIntExtra(MainActivity.EXTRA_TASK_IMPORTANCE, 3).toFloat()
            cbUrgent.isChecked = intent.getBooleanExtra(MainActivity.EXTRA_TASK_IS_URGENT, false)

            when (intent.getStringExtra(MainActivity.EXTRA_TASK_PRIORITY)) {
                getString(R.string.priority_high) -> rbHigh.isChecked = true
                getString(R.string.priority_medium) -> rbMedium.isChecked = true
                getString(R.string.priority_low) -> rbLow.isChecked = true
                else -> rbMedium.isChecked = true // Default
            }
        } else {
            toolbar.title = getString(R.string.add_task_screen_title)
            btnSave.text = getString(R.string.button_save_task)
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            val description = etDesc.text.toString()
            val selectedPriorityId = radioGroup.checkedRadioButtonId
            val priority = if (selectedPriorityId != -1) {
                findViewById<RadioButton>(selectedPriorityId).text.toString()
            } else {
                getString(R.string.priority_medium) // Default si no hay selección
            }
            val importance = ratingBarImportance.rating.toInt()
            val urgent = cbUrgent.isChecked

            val resultIntent = Intent().apply {
                putExtra(MainActivity.EXTRA_TASK_TITLE, title)
                putExtra(MainActivity.EXTRA_TASK_DESCRIPTION, description)
                putExtra(MainActivity.EXTRA_TASK_PRIORITY, priority)
                putExtra(MainActivity.EXTRA_TASK_IMPORTANCE, importance)
                putExtra(MainActivity.EXTRA_TASK_IS_URGENT, urgent)

                if (isEditMode) {
                    putExtra(MainActivity.EXTRA_TASK_ID, currentTaskId)
                    putExtra(MainActivity.EXTRA_TASK_IS_DONE, currentTaskIsDone) // Devolver el estado 'isDone' original
                }
                // Para nuevas tareas, el ID se genera en MainActivity (o en el constructor de Task)
                // y isDone es false por defecto.
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        btnCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // onBackPressed() // Usar onBackPressedDispatcher para consistencia
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
