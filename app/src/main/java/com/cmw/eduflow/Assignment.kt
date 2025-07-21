package com.cmw.eduflow

import com.google.firebase.Timestamp

data class Assignment(
    val id: String = "",
    val title: String = "",
    val dueDate: Timestamp = Timestamp.now(),
    val status: String = "Pending", // e.g., "Pending", "Graded", "Overdue"
    val fileUrl: String = "",
    val teacherId: String = ""
)