package com.cmw.eduflow

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cmw.eduflow.databinding.FragmentTeacherDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class TeacherDashboardFragment : Fragment() {

    private var _binding: FragmentTeacherDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // ✅ Define adapters for the lists
    private lateinit var assignmentAdapter: AssignmentAdapter
    private lateinit var materialAdapter: CourseMaterialAdapter

    private val qrScannerLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(context, "Scan cancelled", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Scanned: ${result.contents}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTeacherDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupToolbar()
        setupRecyclerViews()
        fetchData()

        binding.btnScanQr.setOnClickListener { launchScanner() }
        binding.tvCreateAssignment.setOnClickListener { Toast.makeText(context, "Create Assignment Clicked", Toast.LENGTH_SHORT).show() }
        binding.tvUploadMaterial.setOnClickListener { Toast.makeText(context, "Upload Material Clicked", Toast.LENGTH_SHORT).show() }
    }

    // ✅ New function to set up both RecyclerViews
    private fun setupRecyclerViews() {
        assignmentAdapter = AssignmentAdapter()
        binding.rvAssignments.adapter = assignmentAdapter

        // This assumes you have a CourseMaterialAdapter from the student feature
        materialAdapter = CourseMaterialAdapter()
        binding.rvCourseMaterials.adapter = materialAdapter
    }

    // ✅ New function to fetch all data for the dashboard
    private fun fetchData() {
        setLoading(true)
        // Fetch assignments
        db.collection("assignments")
            .orderBy("dueDate", Query.Direction.DESCENDING)
            .limit(4) // Get the 4 most recent assignments
            .get()
            .addOnSuccessListener { result ->
                val assignments = result.toObjects(Assignment::class.java)
                assignmentAdapter.submitList(assignments)
                // We are still loading, wait for materials to load
            }
            .addOnFailureListener {
                setLoading(false)
                Toast.makeText(context, "Failed to load assignments", Toast.LENGTH_SHORT).show()
            }

        // Fetch course materials
        db.collection("materials")
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .limit(3) // Get the 3 most recent materials
            .get()
            .addOnSuccessListener { result ->
                val materials = result.toObjects(CourseMaterial::class.java)
                materialAdapter.submitList(materials)
                setLoading(false) // Hide loading indicator after all data is fetched
            }
            .addOnFailureListener {
                setLoading(false)
                Toast.makeText(context, "Failed to load materials", Toast.LENGTH_SHORT).show()
            }
    }

    private fun launchScanner() { /* ... unchanged ... */ }
    private fun setupToolbar() { /* ... unchanged ... */ }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}