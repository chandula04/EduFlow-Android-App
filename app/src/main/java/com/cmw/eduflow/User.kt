// com/cmw/eduflow/User.kt
package com.cmw.eduflow

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val phone: String = "",
    val gender: String = "",
    val school: String = "" // "student", "teacher", or "admin" ,phone, gender,school
)