package com.cmw.eduflow

import com.google.firebase.Timestamp

data class CourseMaterial(
    val id: String = "",
    val lessonTitle: String = "",
    val subjectName: String = "",
    val fileUrl: String = "",
    val fileType: String = "", // e.g., "pdf", "video", "image"
    val uploadedAt: Timestamp = Timestamp.now(),
    val teacherId: String = ""
)