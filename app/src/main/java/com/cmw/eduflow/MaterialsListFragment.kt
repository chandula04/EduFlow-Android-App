package com.cmw.eduflow

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cmw.eduflow.databinding.FragmentMaterialsListBinding
import com.cmw.eduflow.databinding.DialogUploadMaterialBinding
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MaterialsListFragment : Fragment() {
    private var _binding: FragmentMaterialsListBinding? = null
    private val binding get() = _binding!!
    private val args: MaterialsListFragmentArgs by navArgs()

    private var selectedFileUri: Uri? = null
    private var tvSelectedFileNameInDialog: TextView? = null
    private lateinit var materialAdapter: CourseMaterialAdapter
    private var currentUserRole: String = "student"

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedFileUri = uri
                tvSelectedFileNameInDialog?.text = "File selected!"
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMaterialsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = args.subjectName
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        fetchCurrentUserRole()
    }

    private fun fetchCurrentUserRole() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            setupUIForRole()
            return
        }

        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    currentUserRole = document.getString("role") ?: "student"
                }
                setupUIForRole()
            }
            .addOnFailureListener {
                setupUIForRole()
            }
    }

    private fun setupUIForRole() {
        if (currentUserRole != "teacher") {
            binding.fabAddMaterial.visibility = View.GONE
        }
        binding.fabAddMaterial.setOnClickListener {
            showUploadMaterialDialog(null)
        }

        materialAdapter = CourseMaterialAdapter(
            userRole = currentUserRole,
            onEditClick = { material -> showUploadMaterialDialog(material) },
            onDeleteClick = { material -> showDeleteMaterialDialog(material) }
        )
        binding.rvMaterials.adapter = materialAdapter

        FirebaseFirestore.getInstance().collection("materials")
            .whereEqualTo("subjectId", args.subjectId)
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, _ ->
                val materials = snapshots?.toObjects(CourseMaterial::class.java)
                materialAdapter.submitList(materials)
            }
    }

    private fun showUploadMaterialDialog(materialToEdit: CourseMaterial? = null) {
        val isEditing = materialToEdit != null
        selectedFileUri = null

        val dialogBinding = DialogUploadMaterialBinding.inflate(LayoutInflater.from(requireContext()))
        tvSelectedFileNameInDialog = dialogBinding.tvSelectedFile

        if (isEditing) {
            dialogBinding.etLessonTitle.setText(materialToEdit!!.lessonTitle)
            dialogBinding.etSubjectName.setText(materialToEdit.subjectName)
            dialogBinding.btnSelectFile.visibility = View.GONE
        } else {
            // Pre-fill subject name if creating a new one
            dialogBinding.etSubjectName.setText(args.subjectName)
        }

        dialogBinding.btnSelectFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*" }
            filePickerLauncher.launch(intent)
        }

        val builder = AlertDialog.Builder(requireContext())
            .setTitle(if (isEditing) "Edit Material Title" else "Upload New Material")
            .setView(dialogBinding.root)

        builder.setPositiveButton(if (isEditing) "Update" else "Upload") { _: DialogInterface, _: Int ->
            val title = dialogBinding.etLessonTitle.text?.toString()?.trim().orEmpty()
            val subject = dialogBinding.etSubjectName.text?.toString()?.trim().orEmpty()
            if (title.isNotEmpty() && subject.isNotEmpty()) {
                if (isEditing) {
                    FirebaseFirestore.getInstance().collection("materials").document(materialToEdit.id)
                        .update(mapOf("lessonTitle" to title, "subjectName" to subject))
                        .addOnSuccessListener { Toast.makeText(context, "Material updated!", Toast.LENGTH_SHORT).show() }
                } else {
                    if (selectedFileUri != null) {
                        uploadFileToCloudinary(title, subject, selectedFileUri!!)
                    } else {
                        Toast.makeText(context, "Please select a file.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Please enter all details.", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun uploadFileToCloudinary(lessonTitle: String, subjectName: String, fileUri: Uri) {
        try {
            MediaManager.get().upload(fileUri)
                .option("resource_type", "auto")
                .option("upload_preset", "eduflow_unsigned")
                .callback(object: UploadCallback {
                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val url = resultData["secure_url"].toString()
                        val fileType = resultData["resource_type"].toString()
                        saveMaterialToFirestore(lessonTitle, subjectName, url, fileType)
                    }
                    override fun onError(requestId: String, error: ErrorInfo) {
                        Toast.makeText(context, "Upload Error: ${error.description}", Toast.LENGTH_LONG).show()
                    }
                    override fun onStart(requestId: String) {}
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                }).dispatch()
        } catch (e: Exception) {
            Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveMaterialToFirestore(lessonTitle: String, subjectName: String, fileUrl: String, fileType: String) {
        val db = FirebaseFirestore.getInstance()
        val materialId = db.collection("materials").document().id
        val teacherId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val material = CourseMaterial(
            id = materialId,
            lessonTitle = lessonTitle,
            subjectName = subjectName,
            fileUrl = fileUrl,
            fileType = if (fileType == "raw") "pdf" else fileType,
            subjectId = args.subjectId,
            uploadedAt = Timestamp.now(),
            teacherId = teacherId
        )

        db.collection("materials").document(materialId).set(material)
            .addOnSuccessListener { Toast.makeText(context, "Material uploaded!", Toast.LENGTH_SHORT).show() }
    }

    private fun showDeleteMaterialDialog(material: CourseMaterial) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Material")
            .setMessage("Are you sure you want to delete '${material.lessonTitle}'?")
            .setPositiveButton("Delete") { _, _ ->
                FirebaseFirestore.getInstance().collection("materials").document(material.id).delete()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}