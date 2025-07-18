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

    private lateinit var assignmentAdapter: AssignmentAdapter
    private lateinit var materialAdapter: CourseMaterialAdapter

    // QR Code Scanner Launcher
    private val qrScannerLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(context, "Scan cancelled", Toast.LENGTH_LONG).show()
        } else {
            // Later, you will use this result to mark attendance
            Toast.makeText(context, "Scanned Student ID: ${result.contents}", Toast.LENGTH_LONG).show()
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

    private fun setupRecyclerViews() {
        assignmentAdapter = AssignmentAdapter()
        binding.rvAssignments.adapter = assignmentAdapter

        materialAdapter = CourseMaterialAdapter()
        binding.rvCourseMaterials.adapter = materialAdapter
    }

    private fun fetchData() {
        setLoading(true)
        db.collection("assignments")
            .orderBy("dueDate", Query.Direction.DESCENDING)
            .limit(4)
            .get()
            .addOnSuccessListener { result ->
                val assignments = result.toObjects(Assignment::class.java)
                assignmentAdapter.submitList(assignments)
            }
            .addOnFailureListener {
                setLoading(false) // Stop loading even if one fails
                Toast.makeText(context, "Failed to load assignments", Toast.LENGTH_SHORT).show()
            }

        db.collection("materials")
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { result ->
                val materials = result.toObjects(CourseMaterial::class.java)
                materialAdapter.submitList(materials)
                setLoading(false) // Hide loading after all data is fetched
            }
            .addOnFailureListener {
                setLoading(false)
                Toast.makeText(context, "Failed to load materials", Toast.LENGTH_SHORT).show()
            }
    }

    // ✅ UPDATED SCANNER LAUNCH FUNCTION
    private fun launchScanner() {
        val options = ScanOptions()
        options.setPrompt("Scan a student's QR code")
        options.setBeepEnabled(true)
        // Tell the scanner to use our new portrait-only activity
        options.setCaptureActivity(CaptureActivityPortrait::class.java)
        options.setOrientationLocked(false) // Allow our activity to control orientation
        qrScannerLauncher.launch(options)
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_profile -> {
                    findNavController().navigate(R.id.action_global_profileFragment)
                    true
                }
                R.id.action_logout -> {
                    val prefs = requireActivity().getSharedPreferences("EduFlowPrefs", Context.MODE_PRIVATE)
                    prefs.edit().putBoolean("isLoggedIn", false).apply()

                    auth.signOut()
                    findNavController().navigate(R.id.homeFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}