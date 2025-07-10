package com.cmw.eduflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cmw.eduflow.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // If user is already logged in from a previous session, navigate them
        if (auth.currentUser != null) {
            checkUserRoleAndNavigate(auth.currentUser?.uid)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                setLoading(true)
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            checkUserRoleAndNavigate(userId)
                        } else {
                            setLoading(false)
                            Toast.makeText(context, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // ✅ ADDED THIS: Handles click on the "Register" text
        binding.tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun checkUserRoleAndNavigate(userId: String?) {
        if (userId == null) {
            setLoading(false) // Not logged in, so stop loading
            return
        }

        setLoading(true)
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role")
                    // Navigate and clear back stack so user can't go back to login screen
                    val action = when (role) {
                        "admin" -> R.id.action_loginFragment_to_adminDashboardFragment
                        "teacher" -> R.id.action_loginFragment_to_teacherDashboardFragment
                        "student" -> R.id.action_loginFragment_to_studentDashboardFragment
                        else -> null
                    }
                    action?.let {
                        findNavController().navigate(it)
                    } ?: run {
                        setLoading(false)
                        Toast.makeText(context, "Unknown user role.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    setLoading(false)
                    // This can happen if auth succeeds but Firestore document is missing
                    Toast.makeText(context, "User data not found. Please register again.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                setLoading(false)
                Toast.makeText(context, "Failed to get user role: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.btnLogin.isEnabled = !isLoading
        binding.tvGoToRegister.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}