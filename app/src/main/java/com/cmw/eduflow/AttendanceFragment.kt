package com.cmw.eduflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cmw.eduflow.databinding.FragmentAttendanceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class AttendanceFragment : Fragment() {
    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var currentUserRole: String = "student"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.rvAttendance.layoutManager = LinearLayoutManager(context)

        fetchUserRoleAndSetupUI()
    }

    private fun fetchUserRoleAndSetupUI() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                currentUserRole = document.getString("role") ?: "student"
            }
            if (currentUserRole == "teacher") {
                binding.calendarView.visibility = View.VISIBLE
                fetchAttendanceForDate(Calendar.getInstance())
                binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                    fetchAttendanceForDate(selectedDate)
                }
            } else {
                binding.calendarView.visibility = View.GONE
                binding.toolbar.title = "My Attendance History"
                fetchStudentAttendance(userId)
            }
        }
    }

    private fun fetchAttendanceForDate(date: Calendar) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = sdf.format(date.time)
        binding.toolbar.subtitle = dateString

        db.collection("attendance")
            .whereEqualTo("dateString", dateString)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                val records = result.toObjects(AttendanceRecord::class.java)
                binding.rvAttendance.adapter = AttendanceAdapter(records)
            }
    }

    private fun fetchStudentAttendance(studentId: String) {
        db.collection("attendance")
            .whereEqualTo("studentId", studentId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val records = result.toObjects(AttendanceRecord::class.java)
                binding.rvAttendance.adapter = AttendanceAdapter(records)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}