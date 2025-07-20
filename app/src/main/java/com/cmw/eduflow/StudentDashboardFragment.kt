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
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class StudentDashboardFragment : Fragment() {

    private var _binding: FragmentStudentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupToolbar()
        setupClickListeners()
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

    private fun setupClickListeners() {
        binding.btnGenerateQr.setOnClickListener {
            generateAndShowQrCode()
        }

        binding.cardViewAssignments.setOnClickListener {
            findNavController().navigate(R.id.action_studentDashboardFragment_to_assignmentsListFragment)
        }

        binding.cardViewMaterials.setOnClickListener {
            findNavController().navigate(R.id.action_studentDashboardFragment_to_subjectsFragment)
        }
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

            val imageView = ImageView(requireContext())
            imageView.setImageBitmap(bitmap)

            AlertDialog.Builder(requireContext())
                .setTitle("Your Attendance QR Code")
                .setView(imageView)
                .setPositiveButton("Close") { dialog, _ ->
                    dialog.dismiss()
                }
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