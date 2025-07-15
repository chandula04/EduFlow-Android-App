package com.cmw.eduflow

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        // Check session when the fragment becomes visible
        checkUserSession()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupAnimations()

        binding.btnGetStarted.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }
    }

    private fun setupAnimations() {
        val animDrawable = binding.mainContainer.background as AnimationDrawable
        animDrawable.setEnterFadeDuration(10)
        animDrawable.setExitFadeDuration(5000)
        animDrawable.start()

        val pulseAnimation = AnimationUtils.loadAnimation(context, R.anim.anim_glow_pulse)
        binding.btnGetStarted.startAnimation(pulseAnimation)
    }

    private fun checkUserSession() {
        val prefs = requireActivity().getSharedPreferences("EduFlowPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
        val user = auth.currentUser

        Log.d("AuthDebug", "Checking session. isLoggedIn flag: $isLoggedIn, currentUser: ${user?.uid}")

        if (isLoggedIn && user != null) {
            navigateToDashboard(user.uid)
        }
    }

    private fun navigateToDashboard(userId: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                // Check if fragment is still added to prevent crash on rapid navigation
                if (isAdded && findNavController().currentDestination?.id == R.id.homeFragment) {
                    if (document != null && document.exists()) {
                        val role = document.getString("role")
                        val action = when (role) {
                            "admin" -> R.id.action_homeFragment_to_adminDashboardFragment
                            "teacher" -> R.id.action_homeFragment_to_teacherDashboardFragment
                            "student" -> R.id.action_homeFragment_to_studentDashboardFragment
                            else -> null
                        }
                        action?.let { findNavController().navigate(it) }
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}