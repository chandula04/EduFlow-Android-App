package com.cmw.eduflow

import android.content.Context
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

    // Define SharedPreferences constants
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

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadCredentials() // Load saved credentials if they exist

        binding.btnLogin.setOnClickListener {
            handleLogin()
        }

        binding.tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        // ✅ ADD THIS
        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }
    }

    // --- New and Updated Functions ---

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            setLoading(true)

            // ✅ HANDLE SAVING CREDENTIALS
            if (binding.cbRememberMe.isChecked) {
                saveCredentials(email, password)
            } else {
                clearCredentials()
            }

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

    // --- Unchanged Functions ---

    private fun checkUserRoleAndNavigate(userId: String?) {
        // ... (this function remains the same)
    }

    private fun setLoading(isLoading: Boolean) {
        // ... (this function remains the same)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}