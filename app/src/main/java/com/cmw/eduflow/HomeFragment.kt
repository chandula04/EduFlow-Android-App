package com.cmw.eduflow

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
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
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        checkUserSession()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Start background animation
        val animDrawable = binding.mainContainer.background as AnimationDrawable
        animDrawable.setEnterFadeDuration(10)
        animDrawable.setExitFadeDuration(5000)
        animDrawable.start()

        // Start button pulse animation
        val pulseAnimation = AnimationUtils.loadAnimation(context, R.anim.anim_glow_pulse)
        binding.btnGetStarted.startAnimation(pulseAnimation)


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
                        if (findNavController().currentDestination?.id == R.id.homeFragment) {
                            if (document != null && document.exists()) {
                                val role = document.getString("role")
                                val action = when (role) {
                                    "admin" -> R.id.action_homeFragment_to_adminDashboardFragment
                                    "teacher" -> R.id.action_homeFragment_to_teacherDashboardFragment
                                    "student" -> R.id.action_homeFragment_to_studentDashboardFragment
                                    else -> null
                                }
                                action?.let {
                                    findNavController().navigate(it)
                                }
                            }
                        }
                    }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}