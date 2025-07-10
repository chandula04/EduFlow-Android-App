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

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val PREFS_NAME = "EduFlowPrefs"
    private val KEY_EMAIL = "email"
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

        // Start background animation
        val animDrawable = view.background as AnimationDrawable
        animDrawable.setEnterFadeDuration(10)
        animDrawable.setExitFadeDuration(5000)
        animDrawable.start()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadCredentials()

        binding.btnLogin.setOnClickListener {
            handleLogin()
        }

        binding.tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            setLoading(true)

            if (binding.cbRememberMe.isChecked) {
                saveCredentials(email, password)
            } else {
                clearCredentials()
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("LoginDebug", "Firebase Auth successful.")
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

    private fun checkUserRoleAndNavigate(userId: String?) {
        if (userId == null) {
            setLoading(false)
            Log.d("LoginDebug", "User ID is null. Cannot proceed.")
            return
        }

        Log.d("LoginDebug", "Checking Firestore for user: $userId")
        setLoading(true)
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                Log.d("LoginDebug", "Firestore call successful.")
                if (document != null && document.exists()) {
                    Log.d("LoginDebug", "Document found.")
                    val role = document.getString("role")
                    Log.d("LoginDebug", "User role from Firestore: $role")

                    val action = when (role) {
                        "admin" -> R.id.action_loginFragment_to_adminDashboardFragment
                        "teacher" -> R.id.action_loginFragment_to_teacherDashboardFragment
                        "student" -> R.id.action_loginFragment_to_studentDashboardFragment
                        else -> null
                    }

                    if (action != null) {
                        Log.d("LoginDebug", "Navigating to the correct dashboard.")
                        findNavController().navigate(action)
                    } else {
                        setLoading(false)
                        Log.d("LoginDebug", "Role is unknown or null. No navigation.")
                        Toast.makeText(context, "Unknown user role.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    setLoading(false)
                    Log.d("LoginDebug", "Document does not exist for this user.")
                    Toast.makeText(context, "User data not found. Please register again.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                setLoading(false)
                Log.e("LoginDebug", "Firestore call failed: ${exception.message}")
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
        prefs.edit().clear().apply()
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