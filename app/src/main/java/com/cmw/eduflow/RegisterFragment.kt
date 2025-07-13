package com.cmw.eduflow

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
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
    private var isPasswordStrong = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val animDrawable = binding.registerContainer.background as AnimationDrawable
        animDrawable.setEnterFadeDuration(10)
        animDrawable.setExitFadeDuration(5000)
        animDrawable.start()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupSpinner()
        setupPasswordStrengthChecker()

        binding.btnRegister.setOnClickListener {
            handleRegistration()
        }
    }

    private fun setupPasswordStrengthChecker() {
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updatePasswordStrength(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updatePasswordStrength(password: String) {
        var strengthScore = 0
        if (password.length >= 8) strengthScore++
        if (password.any { it.isDigit() }) strengthScore++
        if (password.any { it.isUpperCase() }) strengthScore++
        if (password.any { !it.isLetterOrDigit() }) strengthScore++

        val progressDrawable = binding.progressPassword.progressDrawable as LayerDrawable
        val progressClip = progressDrawable.findDrawableByLayerId(android.R.id.progress)

        when (strengthScore) {
            0, 1 -> {
                binding.tvPasswordStrength.text = "Weak"
                binding.progressPassword.progress = 25
                progressClip.setTint(Color.RED)
                isPasswordStrong = false
            }
            2 -> {
                binding.tvPasswordStrength.text = "Medium"
                binding.progressPassword.progress = 50
                progressClip.setTint(Color.YELLOW)
                isPasswordStrong = false
            }
            3 -> {
                binding.tvPasswordStrength.text = "Good"
                binding.progressPassword.progress = 75
                progressClip.setTint(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light))
                isPasswordStrong = false
            }
            4 -> {
                binding.tvPasswordStrength.text = "Strong"
                binding.progressPassword.progress = 100
                progressClip.setTint(Color.GREEN)
                isPasswordStrong = true
            }
        }
    }

    private fun setupSpinner() {
        val roles = listOf("Student", "Teacher")
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item_white, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRole.adapter = adapter
    }

    private fun handleRegistration() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val school = binding.etSchool.text.toString().trim()
        val gradeString = binding.etGrade.text.toString().trim()
        val role = binding.spinnerRole.selectedItem.toString().lowercase()
        val gender = when (binding.rgGender.checkedRadioButtonId) {
            R.id.rbMale -> "Male"
            R.id.rbFemale -> "Female"
            else -> ""
        }

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() || school.isEmpty() || gender.isEmpty() || gradeString.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val grade = gradeString.toIntOrNull()
        if (grade == null || grade !in 1..12) {
            Toast.makeText(context, "Please enter a valid grade (1-12).", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isPasswordStrong) {
            Toast.makeText(context, "Please use a strong password.", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user!!

                firebaseUser.sendEmailVerification().addOnSuccessListener {
                    val customId = (10000000..99999999).random().toString()
                    val user = User(
                        uid = firebaseUser.uid,
                        customId = customId,
                        name = name,
                        email = email,
                        role = role,
                        phone = phone,
                        gender = gender,
                        school = school,
                        grade = grade.toString()
                    )

                    db.collection("users").document(firebaseUser.uid).set(user)
                        .addOnSuccessListener {
                            setLoading(false)
                            showVerificationInfoDialog()
                        }
                        .addOnFailureListener { e ->
                            setLoading(false)
                            Toast.makeText(context, "Error saving user details: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }.addOnFailureListener { e ->
                    setLoading(false)
                    Toast.makeText(context, "Failed to send verification email: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(context, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showVerificationInfoDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Registration Successful")
            .setMessage("A verification link has been sent to your email. Please check your inbox and spam folder to activate your account.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
            .setCancelable(false)
            .show()
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