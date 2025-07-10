package com.cmw.eduflow

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.cmw.eduflow.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
            binding.ivProfile.setImageURI(imageUri)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
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
                    binding.etName.setText(document.getString("name"))
                    binding.etBirthdate.setText(document.getString("birthdate"))
                    when (document.getString("gender")) {
                        "Male" -> binding.rbMale.isChecked = true
                        "Female" -> binding.rbFemale.isChecked = true
                        "Other" -> binding.rbOther.isChecked = true
                    }
                    val photoUrl = document.getString("profilePictureUrl")
                    if (!photoUrl.isNullOrEmpty()) {
                        Glide.with(this).load(photoUrl).into(binding.ivProfile)
                    }
                }
            }
            .addOnFailureListener { setLoading(false) }
    }

    private fun setupClickListeners() {
        binding.tvChangePhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        binding.etBirthdate.setOnClickListener {
            showDatePicker()
        }

        binding.btnUpdateProfile.setOnClickListener {
            updateProfile()
        }

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            binding.etBirthdate.setText("$selectedYear-${selectedMonth + 1}-$selectedDay")
        }, year, month, day).show()
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
                }
                .addOnFailureListener { e ->
                    setLoading(false)
                    Toast.makeText(context, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            saveUserData(userId, null)
        }
    }

    private fun saveUserData(userId: String, imageUrl: String?) {
        val name = binding.etName.text.toString().trim()
        val birthdate = binding.etBirthdate.text.toString().trim()
        val genderId = binding.rgGender.checkedRadioButtonId
        val gender = when (genderId) {
            R.id.rbMale -> "Male"
            R.id.rbFemale -> "Female"
            else -> "Other"
        }

        val userUpdates = mutableMapOf<String, Any>(
            "name" to name,
            "birthdate" to birthdate,
            "gender" to gender
        )
        if (imageUrl != null) {
            userUpdates["profilePictureUrl"] = imageUrl
        }

        db.collection("users").document(userId).update(userUpdates)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
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

        // 1. Delete Firestore document
        db.collection("users").document(userId).delete()
            .addOnSuccessListener {
                // 2. Delete user from Authentication
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