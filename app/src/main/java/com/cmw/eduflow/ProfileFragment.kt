package com.cmw.eduflow

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.cmw.eduflow.databinding.FragmentProfileBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null
    private var isEditMode = false

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            Glide.with(this).load(imageUri).circleCrop().into(binding.ivProfile)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val animDrawable = binding.profileContainer.background as AnimationDrawable
        animDrawable.setEnterFadeDuration(10)
        animDrawable.setExitFadeDuration(5000)
        animDrawable.start()

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        setupToolbar()
        setupGenderSpinner()
        loadUserProfile()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        setLoading(true)
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                setLoading(false)
                if (document.exists()) {
                    val name = document.getString("name")
                    binding.tvNameDisplay.text = name
                    binding.etName.setText(name)
                    binding.tvCustomId.text = "ID: ${document.getString("customId")}"
                    binding.etEmail.setText(document.getString("email"))
                    binding.etPhone.setText(document.getString("phone"))
                    binding.etSchool.setText(document.getString("school"))
                    binding.etGrade.setText(document.getString("grade"))
                    binding.etBirthdate.setText(document.getString("birthdate"))
                    binding.etBio.setText(document.getString("bio"))

                    val gender = document.getString("gender")
                    (binding.spinnerGender.adapter as? ArrayAdapter<String>)?.let { adapter ->
                        val genderPosition = adapter.getPosition(gender)
                        if (genderPosition >= 0) {
                            binding.spinnerGender.setSelection(genderPosition)
                        }
                    }

                    val photoUrl = document.getString("profilePictureUrl")
                    if (!photoUrl.isNullOrEmpty()) {
                        Glide.with(this).load(photoUrl).circleCrop().into(binding.ivProfile)
                    }
                }
            }.addOnFailureListener { setLoading(false) }
    }

    private fun setupGenderSpinner() {
        val genders = listOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item_white, genders)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGender.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnUpdateProfile.setOnClickListener {
            if (isEditMode) {
                handleUpdate()
            } else {
                toggleEditMode(true)
            }
        }

        binding.btnChangeEmail.setOnClickListener {
            showChangeEmailDialog()
        }

        binding.etBirthdate.setOnClickListener {
            if (isEditMode) {
                showDatePickerDialog()
            }
        }
    }

    private fun toggleEditMode(editing: Boolean) {
        isEditMode = editing

        binding.etName.isEnabled = editing
        binding.etPhone.isEnabled = editing
        binding.etSchool.isEnabled = editing
        binding.etGrade.isEnabled = editing
        binding.etBio.isEnabled = editing
        binding.etBirthdate.isEnabled = editing // ✅ ADDED THIS LINE
        binding.spinnerGender.isEnabled = editing

        binding.btnUpdateProfile.text = if (editing) "Save Changes" else "Edit Profile"
        if(isEditMode) {
            binding.etName.requestFocus()
        }
    }

    private fun handleUpdate() {
        val gradeStr = binding.etGrade.text.toString()
        if (binding.layoutGrade.visibility == View.VISIBLE) {
            val grade = gradeStr.toIntOrNull()
            if (grade == null || grade !in 1..12) {
                binding.etGrade.error = "Grade must be between 1 and 12"
                return
            }
        }

        val userId = auth.currentUser?.uid ?: return
        val userUpdates = mapOf(
            "name" to binding.etName.text.toString(),
            "phone" to binding.etPhone.text.toString(),
            "school" to binding.etSchool.text.toString(),
            "birthdate" to binding.etBirthdate.text.toString(),
            "gender" to binding.spinnerGender.selectedItem.toString(),
            "grade" to gradeStr,
            "bio" to binding.etBio.text.toString()
        )

        setLoading(true)
        db.collection("users").document(userId).update(userUpdates)
            .addOnSuccessListener {
                setLoading(false)
                toggleEditMode(false)
                loadUserProfile()
                showSuccessDialog()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDatePickerDialog() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(requireContext(), { _, y, m, d ->
            binding.etBirthdate.setText("$d/${m + 1}/$y")
        }, year, month, day).show()
    }

    private fun showSuccessDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_success, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.btnGotIt).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btnGoHome).setOnClickListener {
            dialog.dismiss()
            findNavController().navigate(R.id.homeFragment)
        }

        dialog.show()
    }

    private fun showChangeEmailDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_email, null)
        val newEmailEditText = dialogView.findViewById<EditText>(R.id.etNewEmail)
        val passwordEditText = dialogView.findViewById<EditText>(R.id.etCurrentPassword)

        AlertDialog.Builder(requireContext())
            .setTitle("Change Email")
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val newEmail = newEmailEditText.text.toString().trim()
                val password = passwordEditText.text.toString().trim()
                if (newEmail.isNotEmpty() && password.isNotEmpty()) {
                    reauthenticateAndChangeEmail(newEmail, password)
                } else {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun reauthenticateAndChangeEmail(newEmail: String, password: String) {
        val user = auth.currentUser ?: return
        val credential = EmailAuthProvider.getCredential(user.email!!, password)

        setLoading(true)
        user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
            if (reauthTask.isSuccessful) {
                user.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener { task ->
                    setLoading(false)
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Verification link sent! Check your new email to finalize the change.", Toast.LENGTH_LONG).show()
                        auth.signOut()
                        findNavController().navigate(R.id.homeFragment)
                    } else {
                        Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                setLoading(false)
                Toast.makeText(context, "Re-authentication failed. Incorrect password.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}