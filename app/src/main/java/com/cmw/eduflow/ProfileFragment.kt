package com.cmw.eduflow

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.cmw.eduflow.databinding.FragmentProfileBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var isEditMode = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // The animation code that caused the crash has been removed.

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupToolbar()
        setupGenderSpinner()
        loadUserProfile()

        binding.btnUpdateProfile.setOnClickListener {
            if (isEditMode) {
                handleUpdate()
            } else {
                toggleEditMode(true) // Enter edit mode
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name")
                    binding.tvNameDisplay.text = name
                    binding.etName.setText(name)
                    binding.tvCustomId.text = "@${document.getString("customId")}"
                    binding.etEmail.setText(document.getString("email"))
                    binding.etBirthdate.setText(document.getString("birthdate"))
                    binding.etGrade.setText(document.getString("grade"))
                    binding.etBio.setText(document.getString("bio"))

                    val gender = document.getString("gender")
                    val genderAdapter = binding.spinnerGender.adapter as ArrayAdapter<String>
                    val genderPosition = genderAdapter.getPosition(gender)
                    binding.spinnerGender.setSelection(genderPosition)

                    val photoUrl = document.getString("profilePictureUrl")
                    if (!photoUrl.isNullOrEmpty()) {
                        Glide.with(this).load(photoUrl).circleCrop().into(binding.ivProfile)
                    }
                }
            }
    }

    private fun setupGenderSpinner() {
        val genders = listOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, genders)
        binding.spinnerGender.adapter = adapter
    }

    private fun toggleEditMode(editing: Boolean) {
        isEditMode = editing

        binding.etName.isEnabled = isEditMode
        binding.etBirthdate.isEnabled = isEditMode
        binding.spinnerGender.isEnabled = isEditMode
        binding.etGrade.isEnabled = isEditMode
        binding.etBio.isEnabled = isEditMode

        binding.btnUpdateProfile.text = if (isEditMode) "Update" else "Edit Profile"
        if(isEditMode) {
            binding.etName.requestFocus()
        }
    }

    private fun handleUpdate() {
        val gradeStr = binding.etGrade.text.toString()
        val grade = gradeStr.toIntOrNull()
        if (grade == null || grade !in 1..12) {
            binding.etGrade.error = "Grade must be between 1 and 12"
            return
        }

        val userId = auth.currentUser?.uid ?: return
        val userUpdates = mapOf(
            "name" to binding.etName.text.toString(),
            "birthdate" to binding.etBirthdate.text.toString(),
            "gender" to binding.spinnerGender.selectedItem.toString(),
            "grade" to gradeStr,
            "bio" to binding.etBio.text.toString()
        )

        db.collection("users").document(userId).update(userUpdates)
            .addOnSuccessListener {
                Toast.makeText(context, "Profile updating...", Toast.LENGTH_SHORT).show()
                toggleEditMode(false) // Exit edit mode
                loadUserProfile() // Reload data to show updated info
                showSuccessDialog()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showSuccessDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_success, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        dialogView.findViewById<Button>(R.id.btnGotIt).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btnGoHome).setOnClickListener {
            dialog.dismiss()
            findNavController().navigate(R.id.homeFragment)
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}