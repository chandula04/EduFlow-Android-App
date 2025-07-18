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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.cmw.eduflow.databinding.FragmentProfileBinding
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    // FirebaseStorage has been removed
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

        binding.ivProfile.setOnClickListener {
            if(isEditMode){
                Intent(Intent.ACTION_PICK).also {
                    it.type = "image/*"
                    imagePickerLauncher.launch(it)
                }
            }
        }
    }

    private fun toggleEditMode(editing: Boolean) {
        isEditMode = editing

        binding.etName.isEnabled = editing
        binding.etPhone.isEnabled = editing
        binding.etSchool.isEnabled = editing
        binding.etBio.isEnabled = editing
        binding.etBirthdate.isEnabled = editing
        binding.spinnerGender.isEnabled = editing

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

        setLoading(true)
        if (imageUri != null) {
            // If a new image was selected, upload it to Cloudinary first
            uploadImageToCloudinaryAndSave()
        } else {
            // If no new image, just save the other data to Firestore
            saveUserDataToFirestore(null)
        }
    }

    private fun uploadImageToCloudinaryAndSave() {
        // IMPORTANT: You must create an unsigned upload preset in your Cloudinary settings.
        MediaManager.get().upload(imageUri)
            .unsigned("YOUR_UPLOAD_PRESET_NAME") // REPLACE WITH YOUR PRESET NAME
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val imageUrl = resultData["secure_url"].toString()
                    // After getting the URL from Cloudinary, save all data to Firestore
                    saveUserDataToFirestore(imageUrl)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    setLoading(false)
                    Toast.makeText(context, "Image upload failed: ${error.description}", Toast.LENGTH_LONG).show()
                }

                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }

    private fun saveUserDataToFirestore(imageUrl: String?) {
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
            userUpdates["grade"] = binding.etGrade.text.toString()
        }

        // If a new image was uploaded, add its Cloudinary URL to the update map
        if (imageUrl != null) {
            userUpdates["profilePictureUrl"] = imageUrl
        }

        db.collection("users").document(userId).update(userUpdates)
            .addOnSuccessListener {
                setLoading(false)
                imageUri = null // Clear the selected image URI after successful upload
                toggleEditMode(false)
                loadUserProfile()
                showSuccessDialog("Profile Updated", "Your changes have been saved successfully!")
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDatePickerDialog() { /* ... unchanged ... */ }
    private fun showSuccessDialog(title: String, message: String) { /* ... unchanged ... */ }
    private fun showChangeEmailDialog() { /* ... unchanged ... */ }
    private fun reauthenticateAndChangeEmail(newEmail: String, password: String) { /* ... unchanged ... */ }
    private fun showDeleteConfirmationDialog() { /* ... unchanged ... */ }
    private fun reauthenticateAndDelete(password: String) { /* ... unchanged ... */ }
    private fun setLoading(isLoading: Boolean) { /* ... unchanged ... */ }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}