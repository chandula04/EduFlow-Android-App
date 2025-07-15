package com.cmw.eduflow

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
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
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("AuthDebug", "HomeFragment onViewCreated") // DEBUG LOG

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupAnimations()
        setupAuthStateListener()

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

    private fun setupAuthStateListener() {
        Log.d("AuthDebug", "Setting up AuthStateListener.") // DEBUG LOG
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in
                Log.d("AuthDebug", "Listener fired: User is SIGNED IN with UID: ${user.uid}") // DEBUG LOG
                navigateToDashboard(user.uid)
            } else {
                // User is signed out
                Log.d("AuthDebug", "Listener fired: User is SIGNED OUT.") // DEBUG LOG
            }
        }
    }

    private fun navigateToDashboard(userId: String) {
        // ... (this function is unchanged)
    }

    override fun onStart() {
        super.onStart()
        Log.d("AuthDebug", "onStart: Adding AuthStateListener.") // DEBUG LOG
        auth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        Log.d("AuthDebug", "onStop: Removing AuthStateListener.") // DEBUG LOG
        auth.removeAuthStateListener(authStateListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}