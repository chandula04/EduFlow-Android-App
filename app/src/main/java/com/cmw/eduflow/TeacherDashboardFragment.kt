package com.cmw.eduflow

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cmw.eduflow.databinding.FragmentTeacherDashboardBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.util.*
import android.app.Activity

class TeacherDashboardFragment : Fragment() {

    private var _binding: FragmentTeacherDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var assignmentAdapter: AssignmentAdapter
    private lateinit var materialAdapter: CourseMaterialAdapter

    private var selectedPdfUri: Uri? = null
    private var selectedDueDate: Calendar = Calendar.getInstance()

    private val pdfPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedPdfUri = result.data?.data
            Toast.makeText(context, "PDF Selected!", Toast.LENGTH_SHORT).show()
        }
    }

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
        storage = FirebaseStorage.getInstance()

        setupToolbar()
        setupRecyclerViews()
        fetchData()

        binding.btnScanQr.setOnClickListener { launchScanner() }

        // ✅ FIXED: Make "Create Now" button functional
        binding.tvCreateAssignment.setOnClickListener { showCreateAssignmentDialog() }

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
            .addSnapshotListener { snapshots, e ->
                if (e != null) { setLoading(false); return@addSnapshotListener }
                val assignments = snapshots?.toObjects(Assignment::class.java)
                assignmentAdapter.submitList(assignments)
            }

        db.collection("materials")
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                setLoading(false)
                if (e != null) { return@addSnapshotListener }
                val materials = snapshots?.toObjects(CourseMaterial::class.java)
                materialAdapter.submitList(materials)
            }
    }

    private fun showCreateAssignmentDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_assignment, null)
        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etAssignmentTitle)
        val tvDueDate = dialogView.findViewById<TextView>(R.id.tvDueDate)
        val btnSelectPdf = dialogView.findViewById<Button>(R.id.btnSelectPdf)
        val tvSelectedFile = dialogView.findViewById<TextView>(R.id.tvSelectedFile)

        tvDueDate.setOnClickListener { showDatePickerDialog(tvDueDate) }

        btnSelectPdf.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/pdf" }
            pdfPickerLauncher.launch(intent)
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val title = etTitle.text.toString().trim()
                if (title.isNotEmpty() && selectedPdfUri != null) {
                    uploadAssignment(title, selectedPdfUri!!, Timestamp(selectedDueDate.time))
                } else {
                    Toast.makeText(context, "Please fill all fields and select a PDF.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun uploadAssignment(title: String, pdfUri: Uri, dueDate: Timestamp) {
        setLoading(true)
        val fileName = "assignments/${System.currentTimeMillis()}_${title.replace(" ", "_")}.pdf"
        val storageRef = storage.reference.child(fileName)

        storageRef.putFile(pdfUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    saveAssignmentToFirestore(title, downloadUrl.toString(), dueDate)
                }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveAssignmentToFirestore(title: String, fileUrl: String, dueDate: Timestamp) {
        val assignmentId = db.collection("assignments").document().id
        val newAssignment = Assignment(
            id = assignmentId,
            title = title,
            dueDate = dueDate,
            status = "Pending"
            // You would also save the fileUrl in your Assignment data class
        )

        db.collection("assignments").document(assignmentId).set(newAssignment)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(context, "Assignment created successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(context, "Failed to create assignment: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDatePickerDialog(tvDueDate: TextView) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(requireContext(), { _, y, m, d ->
            selectedDueDate.set(y, m, d)
            tvDueDate.text = "Due: $d/${m + 1}/$y"
        }, year, month, day).show()
    }

    private fun launchScanner() {
        val options = ScanOptions()
        options.setPrompt("Scan a student's QR code")
        options.setBeepEnabled(true)
        options.setCaptureActivity(CaptureActivityPortrait::class.java)
        options.setOrientationLocked(false)
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