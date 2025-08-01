package com.cmw.eduflow

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
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

    private var _binding: FragmentLoginBinding? = null //lifecycle method to prevent memory leaks when the Fragment's view is destroyed.
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    //define functions
    private val PREFS_NAME = "EduFlowPrefs"//define functions
    private val KEY_EMAIL = "email" //define functions
    private val KEY_PASSWORD = "password"
    private val KEY_REMEMBER = "remember"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val animDrawable = binding.loginContainer.background as AnimationDrawable
        animDrawable.setEnterFadeDuration(10)
        animDrawable.setExitFadeDuration(5000)
        animDrawable.start()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadCredentials()
        setupClickListeners()
    }
 // login button click event
    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener { handleLogin() }
        binding.tvGoToRegister.setOnClickListener { findNavController().navigate(R.id.action_loginFragment_to_registerFragment) }
        binding.tvForgotPassword.setOnClickListener { findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment) }
    }
//login button functions
    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

    // if the pasword and Email is Not Empty , Save the Creatianal Details
        if (email.isNotEmpty() && password.isNotEmpty()) {
            setLoading(true)
            if (binding.cbRememberMe.isChecked) {
                saveCredentials(email, password)
            } else {
                clearCredentials()
            }

            //This use for Verify the email adress 
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null && user.isEmailVerified) {
                            checkUserRoleAndNavigate(user.uid)
                        } else {
                            setLoading(false)
                            Toast.makeText(context, "Please verify your email address.", Toast.LENGTH_LONG).show()
                            auth.signOut()
                        }
                    } else {
                        setLoading(false)
                        Toast.makeText(context, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkUserRoleAndNavigate(userId: String) {
        setLoading(true)
        val user = auth.currentUser ?: return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val authEmail = user.email
                    val firestoreEmail = document.getString("email")
                    if (authEmail != null && authEmail != firestoreEmail) {
                        db.collection("users").document(userId).update("email", authEmail)
                    }

                    val role = document.getString("role")
                    val action = when (role) {
                        "admin" -> R.id.action_loginFragment_to_adminDashboardFragment
                        "teacher" -> R.id.action_loginFragment_to_teacherDashboardFragment
                        "student" -> R.id.action_loginFragment_to_studentDashboardFragment
                        else -> null
                    }

                    if (action != null) {
                        // ✅ SAVE THE LOGGED-IN STATE
                        val prefs = requireActivity().getSharedPreferences("EduFlowPrefs", Context.MODE_PRIVATE)
                        prefs.edit().putBoolean("isLoggedIn", true).apply()

                        findNavController().navigate(action)
                    } else {
                        setLoading(false)
                        Toast.makeText(context, "Unknown user role.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    setLoading(false)
                    Toast.makeText(context, "User data not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                setLoading(false)
                Toast.makeText(context, "Failed to get user role: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveCredentials(email: String, pass: String) {
        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_PASSWORD, pass)
            .putBoolean(KEY_REMEMBER, true)
            .apply()
    }

    private fun loadCredentials() {
        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rememberMe = prefs.getBoolean(KEY_REMEMBER, false)
        if (rememberMe) {
            binding.etEmail.setText(prefs.getString(KEY_EMAIL, ""))
            binding.etPassword.setText(prefs.getString(KEY_PASSWORD, ""))
            binding.cbRememberMe.isChecked = true
        }
    }

    private fun clearCredentials() {
        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_EMAIL).remove(KEY_PASSWORD).remove(KEY_REMEMBER).apply()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.btnLogin.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}