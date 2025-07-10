package com.cmw.eduflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cmw.eduflow.databinding.FragmentTeacherDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TeacherDashboardFragment : Fragment() {

    private var _binding: FragmentTeacherDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        fetchUserData()
        setupClickListeners()
    }

    private fun fetchUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val name = document.getString("name") ?: "Teacher"
                        binding.tvWelcome.text = "Welcome, $name!"
                    }
                }
        }
    }

    private fun setupClickListeners() {
        // Handle Logout
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    auth.signOut()
                    // Navigate to home and clear back stack
                    findNavController().navigate(R.id.homeFragment)
                    true
                }
                else -> false
            }
        }

        // --- Placeholder Click Listeners for Features ---
        binding.cardScanAttendance.setOnClickListener {
            // TODO: Navigate to QR Scanner Fragment
            Toast.makeText(context, "QR Scanner feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.cardManageAssignments.setOnClickListener {
            // TODO: Navigate to Assignments Fragment
            Toast.makeText(context, "Assignment feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.cardUploadMaterials.setOnClickListener {
            // TODO: Navigate to Materials Fragment
            Toast.makeText(context, "Materials feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}