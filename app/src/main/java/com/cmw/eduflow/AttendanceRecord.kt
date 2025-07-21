package com.cmw.eduflow

import com.google.firebase.Timestamp

data class AttendanceRecord(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    // We save the date as a simple string for easy querying
    val dateString: String = ""
)