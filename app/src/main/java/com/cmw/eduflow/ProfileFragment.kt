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
import android.widget.TextView

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
                    binding.etBirthdate.setText(document.getString("birthdate"))
                    binding.etBio.setText(document.getString("bio"))

                    val role = document.getString("role")
                    if (role == "teacher") {
                        binding.layoutGrade.visibility = View.GONE
                    } else {
                        binding.layoutGrade.visibility = View.VISIBLE
                        binding.etGrade.setText(document.getString("grade"))
                    }

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

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun toggleEditMode(editing: Boolean) {
        isEditMode = editing

        binding.etName.isEnabled = editing
        binding.etPhone.isEnabled = editing
        binding.etSchool.isEnabled = editing
        binding.etBio.isEnabled = editing
        binding.spinnerGender.isEnabled = editing

        // ✅ FIX: This enables the birthday field so it can be clicked
        binding.etBirthdate.isEnabled = editing

        if (binding.layoutGrade.visibility == View.VISIBLE) {
            binding.etGrade.isEnabled = editing
        }

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
        val userUpdates = mutableMapOf<String, Any>(
            "name" to binding.etName.text.toString(),
            "phone" to binding.etPhone.text.toString(),
            "school" to binding.etSchool.text.toString(),
            "birthdate" to binding.etBirthdate.text.toString(),
            "gender" to binding.spinnerGender.selectedItem.toString(),
            "bio" to binding.etBio.text.toString()
        )

        if (binding.layoutGrade.visibility == View.VISIBLE) {
            userUpdates["grade"] = gradeStr
        }

        setLoading(true)
        db.collection("users").document(userId).update(userUpdates)
            .addOnSuccessListener {
                setLoading(false)
                toggleEditMode(false)
                loadUserProfile()
                showSuccessDialog("Profile Updated", "Your changes have been saved successfully!")
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

    private fun showSuccessDialog(title: String, message: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_success, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        val successTitle = dialogView.findViewById<TextView>(R.id.tvSuccessTitle)
        val successMessage = dialogView.findViewById<TextView>(R.id.tvSuccessMessage)
        successTitle.text = title
        successMessage.text = message

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
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_email, null)
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
                        Toast.makeText(context, "Verification link sent! Check your new email and log in again.", Toast.LENGTH_LONG).show()
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

    private fun showDeleteConfirmationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reauthenticate, null)
        val passwordEditText = dialogView.findViewById<EditText>(R.id.etPassword)

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("This is permanent. Please enter your password to confirm.")
            .setView(dialogView)
            .setPositiveButton("Delete Forever") { _, _ ->
                val password = passwordEditText.text.toString()
                if (password.isNotEmpty()) {
                    reauthenticateAndDelete(password)
                } else {
                    Toast.makeText(context, "Password is required.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun reauthenticateAndDelete(password: String) {
        setLoading(true)
        val user = auth.currentUser ?: return
        val credential = EmailAuthProvider.getCredential(user.email!!, password)

        user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
            if (reauthTask.isSuccessful) {
                db.collection("users").document(user.uid).delete()
                    .addOnCompleteListener { firestoreTask ->
                        if(firestoreTask.isSuccessful) {
                            user.delete().addOnCompleteListener { authTask ->
                                setLoading(false)
                                if(authTask.isSuccessful) {
                                    Toast.makeText(context, "Account permanently deleted.", Toast.LENGTH_SHORT).show()
                                    findNavController().navigate(R.id.homeFragment)
                                } else {
                                    Toast.makeText(context, "Account deletion failed. Please try again.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            setLoading(false)
                            Toast.makeText(context, "Could not delete user data. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                setLoading(false)
                Toast.makeText(context, "Incorrect password. Deletion cancelled.", Toast.LENGTH_SHORT).show()
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