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

                    val role = document.getString("role")
                    if (role == "teacher") {
                        binding.layoutGrade.visibility = View.GONE
                    } else {
                        binding.layoutGrade.visibility = View.VISIBLE
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

        // ✅ FIXED: ADDED CLICK LISTENER
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

        // ✅ FIXED: ENABLE BIRTHDATE FIELD IN EDIT MODE
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
        // ... (this function is unchanged)
    }

    private fun showDatePickerDialog() {
        // ... (this function is unchanged)
    }

    private fun showSuccessDialog() {
        // ... (this function is unchanged)
    }

    private fun showChangeEmailDialog() {
        // ... (this function is unchanged)
    }

    private fun reauthenticateAndChangeEmail(newEmail: String, password: String) {
        // ... (this function is unchanged)
    }

    private fun showDeleteConfirmationDialog() {
        // ... (this function is unchanged)
    }

    private fun reauthenticateAndDelete(password: String) {
        // ... (this function is unchanged)
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}