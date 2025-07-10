package com.cmw.eduflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cmw.eduflow.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Check for existing user session as soon as the view is created
        checkUserSession()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the click listener for users who are not logged in
        binding.btnGetStarted.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }
    }

    private fun checkUserSession() {
        if (auth.currentUser != null) {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                db.collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
                        // ✅ THIS IS THE FIX:
                        // Only navigate if the current screen is still the HomeFragment.
                        // This prevents a crash if the user navigates away before this check completes.
                        if (findNavController().currentDestination?.id == R.id.homeFragment) {
                            if (document != null && document.exists()) {
                                val role = document.getString("role")
                                val action = when (role) {
                                    "admin" -> R.id.action_homeFragment_to_adminDashboardFragment
                                    "teacher" -> R.id.action_homeFragment_to_teacherDashboardFragment
                                    "student" -> R.id.action_homeFragment_to_studentDashboardFragment
                                    else -> null // Do nothing if role is unknown
                                }
                                action?.let {
                                    findNavController().navigate(it)
                                }
                            }
                        }
                    }
            }
        }
        // If auth.currentUser is null, do nothing. The user will stay on the home screen.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}