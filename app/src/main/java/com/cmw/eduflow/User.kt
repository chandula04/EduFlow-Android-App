// com/cmw/eduflow/User.kt
package com.cmw.eduflow

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "" // "student", "teacher", or "admin"
)