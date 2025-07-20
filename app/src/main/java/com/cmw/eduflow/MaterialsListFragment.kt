package com.cmw.eduflow

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cmw.eduflow.databinding.FragmentMaterialsListBinding
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MaterialsListFragment : Fragment() {
    private var _binding: FragmentMaterialsListBinding? = null
    private val binding get() = _binding!!
    private val args: MaterialsListFragmentArgs by navArgs()

    private var selectedFileUri: Uri? = null
    private var tvSelectedFileNameInDialog: TextView? = null
    private lateinit var materialAdapter: CourseMaterialAdapter

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

        setupRecyclerView()

        binding.fabAddMaterial.setOnClickListener {
            showUploadMaterialDialog(null)
        }

        FirebaseFirestore.getInstance().collection("materials")
            .whereEqualTo("subjectId", args.subjectId)
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, _ ->
                val materials = snapshots?.toObjects(CourseMaterial::class.java)
                materialAdapter.submitList(materials)
            }
    }

    private fun setupRecyclerView() {
        materialAdapter = CourseMaterialAdapter(
            onEditClick = { material ->
                showUploadMaterialDialog(material)
            },
            onDeleteClick = { material ->
                showDeleteMaterialDialog(material)
            }
        )
        binding.rvMaterials.adapter = materialAdapter
    }

    private fun showUploadMaterialDialog(materialToEdit: CourseMaterial?) {
        val isEditing = materialToEdit != null
        selectedFileUri = null

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_upload_material, null)
        val etLessonTitle = dialogView.findViewById<EditText>(R.id.etLessonTitle)
        val btnSelectFile = dialogView.findViewById<Button>(R.id.btnSelectFile)
        tvSelectedFileNameInDialog = dialogView.findViewById(R.id.tvSelectedFile)

        if (isEditing) {
            etLessonTitle.setText(materialToEdit?.lessonTitle)
            btnSelectFile.visibility = View.GONE
            tvSelectedFileNameInDialog?.visibility = View.GONE
        }

        btnSelectFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*" }
            filePickerLauncher.launch(intent)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (isEditing) "Edit Material Title" else "Upload New Material")
            .setView(dialogView)
            .setPositiveButton(if (isEditing) "Update" else "Upload") { _, _ ->
                val title = etLessonTitle.text.toString().trim()
                if (title.isNotEmpty()) {
                    if (isEditing) {
                        FirebaseFirestore.getInstance().collection("materials").document(materialToEdit!!.id)
                            .update("lessonTitle", title)
                            .addOnSuccessListener { Toast.makeText(context, "Material updated!", Toast.LENGTH_SHORT).show() }
                    } else {
                        if (selectedFileUri != null) {
                            uploadFileToCloudinary(title, selectedFileUri!!)
                        } else {
                            Toast.makeText(context, "Please select a file.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Please enter a title.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun uploadFileToCloudinary(lessonTitle: String, fileUri: Uri) {
        // ✅ CORRECTED UPLOAD LOGIC
        MediaManager.get().upload(fileUri)
            .unsigned("eduflow_unsigned")
            .option("resource_type", "auto")
            .callback(object: UploadCallback {
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"].toString()
                    val fileType = resultData["resource_type"].toString()
                    saveMaterialToFirestore(lessonTitle, url, fileType)
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    Toast.makeText(context, "Upload Error: ${error.description}", Toast.LENGTH_LONG).show()
                }
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }

    private fun saveMaterialToFirestore(lessonTitle: String, fileUrl: String, fileType: String) {
        val db = FirebaseFirestore.getInstance()
        val materialId = db.collection("materials").document().id
        val material = CourseMaterial(
            id = materialId,
            lessonTitle = lessonTitle,
            fileUrl = fileUrl,
            fileType = if (fileType == "raw") "pdf" else fileType,
            subjectName = args.subjectName,
            uploadedAt = Timestamp.now()
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