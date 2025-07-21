package com.cmw.eduflow

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cmw.eduflow.databinding.FragmentStudentDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class StudentDashboardFragment : Fragment() {

    private var _binding: FragmentStudentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var materialAdapter: CourseMaterialAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStudentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
//function define
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupToolbar()
        setupRecyclerView()
        fetchData()
        setupClickListeners()
    }
//logout and profile edit button
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

    private fun setupRecyclerView() {
        // This tells the adapter the user is a "student" so it hides the edit/delete buttons
        materialAdapter = CourseMaterialAdapter(
            userRole = "student",
            onEditClick = { /* Students cannot edit */ },
            onDeleteClick = { /* Students cannot delete */ }
        )
        binding.rvCourseMaterials.adapter = materialAdapter
    }
//coursework upload (cloudinary)
    private fun fetchData() {
        db.collection("materials")
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) { return@addSnapshotListener }
                val materials = snapshots?.toObjects(CourseMaterial::class.java)
                materialAdapter.submitList(materials)
            }
    }

    private fun setupClickListeners() {
        binding.btnGenerateQr.setOnClickListener {
            generateAndShowQrCode()
        }

        binding.cardViewAssignments.setOnClickListener {
            findNavController().navigate(R.id.action_studentDashboardFragment_to_assignmentsListFragment)
        }

        binding.cardViewResults.setOnClickListener {
            showResultsDialog()
        }
    }

    private fun showResultsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Results")
            .setMessage("The results feature is coming soon!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun generateAndShowQrCode() {
        val studentId = auth.currentUser?.uid
        if (studentId == null) {
            Toast.makeText(context, "Could not generate QR Code. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(studentId, BarcodeFormat.QR_CODE, 400, 400)
            val imageView = ImageView(requireContext()).apply { setImageBitmap(bitmap) }
            AlertDialog.Builder(requireContext())
                .setTitle("Your Attendance QR Code")
                .setView(imageView)
                .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error generating QR Code.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}