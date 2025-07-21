package com.cmw.eduflow

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

        fetchUserRoleAndData()
    }

    private fun fetchUserRoleAndData() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                currentUserRole = document.getString("role") ?: "student"
            }

            // Now that we have the role, fetch the appropriate data
            if (currentUserRole == "teacher") {
                binding.toolbar.title = "All Attendance Records"
                fetchAllAttendance()
            } else {
                binding.toolbar.title = "My Attendance History"
                fetchStudentAttendance(userId)
            }
        }
    }

    private fun fetchAllAttendance() {
        db.collection("attendance")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val records = result.toObjects(AttendanceRecord::class.java)
                binding.rvAttendance.adapter = AttendanceAdapter(records, "teacher") { recordToDelete ->
                    showDeleteAttendanceDialog(recordToDelete)
                }
            }
    }

    private fun fetchStudentAttendance(studentId: String) {
        db.collection("attendance")
            .whereEqualTo("studentId", studentId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val records = result.toObjects(AttendanceRecord::class.java)
                binding.rvAttendance.adapter = AttendanceAdapter(records, "student") { /* Students can't delete */ }
            }
    }

    private fun showDeleteAttendanceDialog(record: AttendanceRecord) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Attendance")
            .setMessage("Are you sure you want to delete the record for ${record.studentName}?")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("attendance").document(record.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Record deleted.", Toast.LENGTH_SHORT).show()
                        fetchAllAttendance() // Refresh the list
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}