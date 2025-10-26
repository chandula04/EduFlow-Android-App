package com.cmw.eduflow

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cmw.eduflow.databinding.FragmentAdminDashboardBinding
import com.google.firebase.auth.FirebaseAuth

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

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
                    prefs.edit {
                        putBoolean("isLoggedIn", false)
                    }
                    auth.signOut()
                    findNavController().navigate(R.id.homeFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnManageStudents.setOnClickListener {
            Toast.makeText(context, "Student Management feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnManageTeachers.setOnClickListener {
            Toast.makeText(context, "Teacher Management feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.cardViewAssignStudents.setOnClickListener {
            Toast.makeText(context, "Assign Students feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.cardViewManageSubjects.setOnClickListener {
            Toast.makeText(context, "Manage Subjects feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.cardViewAttendanceReports.setOnClickListener {
            Toast.makeText(context, "Attendance Reports feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.cardViewResultsReports.setOnClickListener {
            Toast.makeText(context, "Performance Reports feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.cardViewSystemSettings.setOnClickListener {
            Toast.makeText(context, "System Settings feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}