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

        // ✅ CORRECTED: Get the background from the correct container ID
        val animDrawable = binding.profileContainer.background as AnimationDrawable
        animDrawable.setEnterFadeDuration(10)
        animDrawable.setExitFadeDuration(5000)
        animDrawable.start()

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        loadUserProfile()
        setupClickListeners()
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        setLoading(true)
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                setLoading(false)
                if (document.exists()) {
                    binding.tvId.text = document.getString("customId")
                    binding.tvEmail.text = document.getString("email")
                    val role = document.getString("role")
                    binding.tvRole.text = role?.replaceFirstChar { it.uppercase() }

                    if(role == "teacher") {
                        binding.layoutGrade.visibility = View.GONE
                        binding.ivEditGrade.visibility = View.GONE
                    } else {
                        binding.layoutGrade.visibility = View.VISIBLE
                        binding.ivEditGrade.visibility = View.VISIBLE
                        binding.etGrade.setText(document.getString("grade"))
                    }

                    binding.etName.setText(document.getString("name"))
                    binding.etPhone.setText(document.getString("phone"))
                    binding.etSchool.setText(document.getString("school"))

                    when (document.getString("gender")) {
                        "Male" -> binding.rbMale.isChecked = true
                        "Female" -> binding.rbFemale.isChecked = true
                        "Other" -> binding.rbOther.isChecked = true
                    }
                    val photoUrl = document.getString("profilePictureUrl")
                    if (!photoUrl.isNullOrEmpty()) {
                        Glide.with(this).load(photoUrl).circleCrop().into(binding.ivProfile)
                    }
                }
            }.addOnFailureListener { setLoading(false) }
    }

    private fun setupClickListeners() {
        binding.ivEditName.setOnClickListener { toggleEdit(binding.etName) }
        binding.ivEditPhone.setOnClickListener { toggleEdit(binding.etPhone) }
        binding.ivEditSchool.setOnClickListener { toggleEdit(binding.etSchool) }
        binding.ivEditGrade.setOnClickListener { toggleEdit(binding.etGrade) }
        binding.ivEditGender.setOnClickListener {
            binding.rbMale.isEnabled = !binding.rbMale.isEnabled
            binding.rbFemale.isEnabled = !binding.rbFemale.isEnabled
            binding.rbOther.isEnabled = !binding.rbOther.isEnabled
        }

        binding.tvChangePhoto.setOnClickListener {
            Intent(Intent.ACTION_PICK).also {
                it.type = "image/*"
                imagePickerLauncher.launch(it)
            }
        }

        binding.btnChangeEmail.setOnClickListener {
            showChangeEmailDialog()
        }

        binding.btnUpdateProfile.setOnClickListener {
            updateProfile()
        }

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun toggleEdit(editText: EditText) {
        editText.isEnabled = !editText.isEnabled
        if (editText.isEnabled) {
            editText.requestFocus()
        }
    }

    private fun showChangeEmailDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_email, null)
        val newEmailEditText = dialogView.findViewById<EditText>(R.id.etNewEmail)
        val passwordEditText = dialogView.findViewById<EditText>(R.id.etCurrentPassword)

        builder.setView(dialogView)
            .setTitle("Change Email")
            .setPositiveButton("Submit") { _, _ ->
                val newEmail = newEmailEditText.text.toString().trim()
                val password = passwordEditText.text.toString().trim()
                if (newEmail.isNotEmpty() && password.isNotEmpty()) {
                    reauthenticateAndChangeEmail(newEmail, password)
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
                user.updateEmail(newEmail).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        val updates = mapOf("email" to newEmail)
                        db.collection("users").document(user.uid).update(updates).addOnCompleteListener {
                            user.sendEmailVerification().addOnCompleteListener {
                                setLoading(false)
                                Toast.makeText(context, "Email changed. Please check your new email address to verify it.", Toast.LENGTH_LONG).show()
                                auth.signOut()
                                findNavController().navigate(R.id.homeFragment)
                            }
                        }
                    } else {
                        setLoading(false)
                        Toast.makeText(context, "Failed to update email: ${updateTask.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                setLoading(false)
                Toast.makeText(context, "Re-authentication failed. Please check your password.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateProfile() {
        val userId = auth.currentUser?.uid ?: return
        setLoading(true)

        if (imageUri != null) {
            val storageRef = storage.reference.child("profile_pictures/$userId")
            storageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        saveUserData(userId, uri.toString())
                    }
                }.addOnFailureListener { e ->
                    setLoading(false)
                    Toast.makeText(context, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            saveUserData(userId, null)
        }
    }

    private fun saveUserData(userId: String, imageUrl: String?) {
        val userUpdates = mutableMapOf<String, Any>()
        if (binding.etName.isEnabled) userUpdates["name"] = binding.etName.text.toString()
        if (binding.etPhone.isEnabled) userUpdates["phone"] = binding.etPhone.text.toString()
        if (binding.etSchool.isEnabled) userUpdates["school"] = binding.etSchool.text.toString()
        if (binding.etGrade.isEnabled) userUpdates["grade"] = binding.etGrade.text.toString()
        if (binding.rbMale.isEnabled) {
            userUpdates["gender"] = when (binding.rgGender.checkedRadioButtonId) {
                R.id.rbMale -> "Male"
                R.id.rbFemale -> "Female"
                else -> "Other"
            }
        }
        imageUrl?.let { userUpdates["profilePictureUrl"] = it }

        if (userUpdates.isEmpty()) {
            setLoading(false)
            Toast.makeText(context, "No changes to update.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(userId).update(userUpdates)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action is permanent and cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteAccount() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAccount() {
        setLoading(true)
        val user = auth.currentUser ?: return
        val userId = user.uid

        db.collection("users").document(userId).delete()
            .addOnSuccessListener {
                user.delete()
                    .addOnSuccessListener {
                        setLoading(false)
                        Toast.makeText(context, "Account deleted successfully.", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.homeFragment)
                    }
                    .addOnFailureListener { e ->
                        setLoading(false)
                        Toast.makeText(context, "Failed to delete account auth: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(context, "Failed to delete user data: ${e.message}", Toast.LENGTH_LONG).show()
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