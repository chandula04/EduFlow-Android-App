package com.cmw.eduflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cmw.eduflow.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // The animation code that caused the crash has been removed from here.

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupSpinner()

        binding.btnRegister.setOnClickListener {
            handleRegistration()
        }
    }

    private fun setupSpinner() {
        val roles = listOf("Student", "Teacher")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, roles)
        binding.spinnerRole.adapter = adapter
    }

    private fun handleRegistration() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val school = binding.etSchool.text.toString().trim()
        val role = binding.spinnerRole.selectedItem.toString().lowercase()
        val gender = when (binding.rgGender.checkedRadioButtonId) {
            R.id.rbMale -> "Male"
            R.id.rbFemale -> "Female"
            else -> ""
        }

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() || school.isEmpty() || gender.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user!!

                val user = User(
                    uid = firebaseUser.uid,
                    name = name,
                    email = email,
                    role = role,
                    phone = phone,
                    gender = gender,
                    school = school
                )

                db.collection("users").document(firebaseUser.uid).set(user)
                    .addOnSuccessListener {
                        setLoading(false)
                        Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                    }
                    .addOnFailureListener { e ->
                        setLoading(false)
                        Toast.makeText(context, "Error saving user details: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(context, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.btnRegister.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}