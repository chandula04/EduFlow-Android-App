package com.cmw.eduflow

import com.google.firebase.Timestamp
import java.util.Date

data class CourseMaterial(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val fileUrl: String = "",
    // Use a default value for the timestamp
    val uploadedAt: Timestamp = Timestamp(Date())
)