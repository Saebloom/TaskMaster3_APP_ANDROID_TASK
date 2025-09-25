package com.example.taskmaster.model

import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(), // ID único autogenerado
    var title: String, // Cambiado a var
    var description: String, // Cambiado a var
    var isDone: Boolean = false,
    var priority: String = "Media",
    var importance: Int = 3,
    var isUrgent: Boolean = false
)
