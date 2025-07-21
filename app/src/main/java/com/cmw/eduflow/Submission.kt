package com.cmw.eduflow

import com.google.firebase.Timestamp

data class Submission(
    val id: String = "",
    val assignmentId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val fileUrl: String = "",
    val submittedAt: Timestamp = Timestamp.now()
)