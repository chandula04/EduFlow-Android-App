package com.cmw.eduflow

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cmw.eduflow.databinding.FragmentTeacherDashboardBinding
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.util.*

class TeacherDashboardFragment : Fragment() {

    private var _binding: FragmentTeacherDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var assignmentAdapter: AssignmentAdapter
    private lateinit var materialAdapter: CourseMaterialAdapter

    private var selectedFileUri: Uri? = null
    private var selectedDueDate: Calendar = Calendar.getInstance()
    private var tvSelectedFileNameInDialog: TextView? = null

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedFileUri = uri
                tvSelectedFileNameInDialog?.text = "File selected!"
            }
        }
    }

    private val qrScannerLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(context, "Scan cancelled", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Scanned: ${result.contents}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTeacherDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupToolbar()
        setupRecyclerViews()
        fetchData()

        binding.btnScanQr.setOnClickListener { launchScanner() }
        binding.tvCreateAssignment.setOnClickListener { showCreateAssignmentDialog() }
        binding.tvUploadMaterial.setOnClickListener { showUploadMaterialDialog(null) }
    }

    private fun setupRecyclerViews() {
        assignmentAdapter = AssignmentAdapter(
            userRole = "teacher",
            onEditClick = { assignment -> showCreateAssignmentDialog(assignment) },
            onDeleteClick = { assignment -> showDeleteConfirmationDialog(assignment) },
            onUploadClick = { /* Teachers do not upload answers */ },
            onSubmissionClick = { /* Teachers do not view submissions this way */ }
        )
        binding.rvAssignments.adapter = assignmentAdapter

        materialAdapter = CourseMaterialAdapter(
            userRole = "teacher",
            onEditClick = { material -> showUploadMaterialDialog(material) },
            onDeleteClick = { material -> showDeleteMaterialDialog(material) }
        )
        binding.rvCourseMaterials.adapter = materialAdapter
    }

    private fun fetchData() {
        setLoading(true)
        val userId = auth.currentUser?.uid
        if (userId == null) {
            setLoading(false)
            return
        }

        db.collection("assignments")
            .whereEqualTo("teacherId", userId)
            .orderBy("dueDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) { return@addSnapshotListener }
                val assignments = snapshots?.toObjects(Assignment::class.java)
                // Convert the list to the correct type for the adapter
                val assignmentPairs = assignments?.map { Pair(it, null as Submission?) }
                assignmentAdapter.submitList(assignmentPairs)
            }

        db.collection("materials")
            .whereEqualTo("teacherId", userId)
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                setLoading(false)
                if (e != null) { return@addSnapshotListener }
                val materials = snapshots?.toObjects(CourseMaterial::class.java)
                materialAdapter.submitList(materials)
            }
    }

    private fun showCreateAssignmentDialog(assignmentToEdit: Assignment? = null) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_assignment, null)
        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etAssignmentTitle)
        val tvDueDate = dialogView.findViewById<TextView>(R.id.tvDueDate)
        val btnSelectPdf = dialogView.findViewById<Button>(R.id.btnSelectPdf)
        tvSelectedFileNameInDialog = dialogView.findViewById(R.id.tvSelectedFile)

        val isEditing = assignmentToEdit != null
        if (isEditing) {
            etTitle.setText(assignmentToEdit?.title)
            btnSelectPdf.visibility = View.GONE
            tvSelectedFileNameInDialog?.visibility = View.GONE
        }

        tvDueDate.setOnClickListener { showDatePickerDialog(tvDueDate) }
        btnSelectPdf.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/pdf" }
            filePickerLauncher.launch(intent)
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle(if (isEditing) "Edit Assignment" else "Create Assignment")
            .setPositiveButton(if (isEditing) "Update" else "Create") { _, _ ->
                val title = etTitle.text.toString().trim()
                if (title.isNotEmpty()) {
                    if (isEditing) {
                        val updatedData = mapOf("title" to title, "dueDate" to Timestamp(selectedDueDate.time))
                        db.collection("assignments").document(assignmentToEdit!!.id).update(updatedData)
                            .addOnSuccessListener { Toast.makeText(context, "Assignment updated!", Toast.LENGTH_SHORT).show() }
                    } else {
                        if (selectedFileUri != null) {
                            uploadPdfToCloudinaryAndSave(title, selectedFileUri!!, Timestamp(selectedDueDate.time))
                        } else {
                            Toast.makeText(context, "Please select a PDF.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Please enter a title.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun uploadPdfToCloudinaryAndSave(title: String, pdfUri: Uri, dueDate: Timestamp) {
        setLoading(true)
        MediaManager.get().upload(pdfUri)
            .option("resource_type", "auto")
            .unsigned("eduflow_unsigned")
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val fileUrl = resultData["secure_url"].toString()
                    saveAssignmentToFirestore(title, fileUrl, dueDate)
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    setLoading(false)
                    Toast.makeText(context, "Upload Error: ${error.description}", Toast.LENGTH_LONG).show()
                }
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }

    private fun saveAssignmentToFirestore(title: String, fileUrl: String, dueDate: Timestamp) {
        val userId = auth.currentUser?.uid ?: return
        val assignmentId = db.collection("assignments").document().id
        val newAssignment = Assignment(
            id = assignmentId,
            title = title,
            dueDate = dueDate,
            status = "Pending",
            fileUrl = fileUrl,
            teacherId = userId
        )
        db.collection("assignments").document(assignmentId).set(newAssignment)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(context, "Assignment created successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(context, "Failed to create assignment: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteConfirmationDialog(assignment: Assignment) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Assignment")
            .setMessage("Are you sure you want to delete '${assignment.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("assignments").document(assignment.id).delete()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showUploadMaterialDialog(materialToEdit: CourseMaterial? = null) {
        val isEditing = materialToEdit != null
        selectedFileUri = null
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_upload_material, null)
        val etLessonTitle = dialogView.findViewById<EditText>(R.id.etLessonTitle)
        val etSubjectName = dialogView.findViewById<EditText>(R.id.etSubjectName)
        val btnSelectFile = dialogView.findViewById<Button>(R.id.btnSelectFile)
        tvSelectedFileNameInDialog = dialogView.findViewById(R.id.tvSelectedFile)

        if (isEditing) {
            etLessonTitle.setText(materialToEdit?.lessonTitle)
            etSubjectName.setText(materialToEdit?.subjectName)
            btnSelectFile.visibility = View.GONE
        }

        btnSelectFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*" }
            filePickerLauncher.launch(intent)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (isEditing) "Edit Material" else "Upload New Material")
            .setView(dialogView)
            .setPositiveButton(if (isEditing) "Update" else "Upload") { _, _ ->
                val lessonTitle = etLessonTitle.text.toString().trim()
                val subjectName = etSubjectName.text.toString().trim()
                if (lessonTitle.isNotEmpty() && subjectName.isNotEmpty()) {
                    if (isEditing) {
                        val updatedData = mapOf("lessonTitle" to lessonTitle, "subjectName" to subjectName)
                        db.collection("materials").document(materialToEdit!!.id).update(updatedData)
                    } else {
                        if (selectedFileUri != null) {
                            uploadMaterialToCloudinary(lessonTitle, subjectName, selectedFileUri!!)
                        } else {
                            Toast.makeText(context, "Please select a file.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Please fill all fields.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun uploadMaterialToCloudinary(lessonTitle: String, subjectName: String, fileUri: Uri) {
        setLoading(true)
        MediaManager.get().upload(fileUri)
            .unsigned("eduflow_unsigned")
            .callback(object: UploadCallback {
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"].toString()
                    val fileType = resultData["resource_type"].toString()
                    saveMaterialToFirestore(lessonTitle, subjectName, url, fileType)
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    setLoading(false)
                    Toast.makeText(context, "Upload Error: ${error.description}", Toast.LENGTH_LONG).show()
                }
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }

    private fun saveMaterialToFirestore(lessonTitle: String, subjectName: String, fileUrl: String, fileType: String) {
        val userId = auth.currentUser?.uid ?: return
        val materialId = db.collection("materials").document().id
        val material = CourseMaterial(
            id = materialId,
            lessonTitle = lessonTitle,
            subjectName = subjectName,
            fileUrl = fileUrl,
            fileType = if (fileType == "raw") "pdf" else fileType,
            uploadedAt = Timestamp.now(),
            teacherId = userId
        )
        db.collection("materials").document(materialId).set(material)
            .addOnSuccessListener { setLoading(false); Toast.makeText(context, "Material uploaded!", Toast.LENGTH_SHORT).show() }
    }

    private fun showDeleteMaterialDialog(material: CourseMaterial) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Material")
            .setMessage("Are you sure you want to delete '${material.lessonTitle}'?")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("materials").document(material.id).delete()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDatePickerDialog(tvDueDate: TextView) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(requireContext(), { _, y, m, d ->
            selectedDueDate.set(y, m, d)
            tvDueDate.text = "Due: $d/${m + 1}/$y"
        }, year, month, day).show()
    }

    private fun launchScanner() {
        val options = ScanOptions()
        options.setPrompt("Scan a student's QR code")
        options.setBeepEnabled(true)
        options.setCaptureActivity(CaptureActivityPortrait::class.java)
        options.setOrientationLocked(false)
        qrScannerLauncher.launch(options)
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_profile -> {
                    findNavController().navigate(R.id.action_global_profileFragment)
                    true
                }
                R.id.action_logout -> {
                    val prefs = requireActivity().getSharedPreferences("EduFlowPrefs", Context.MODE_PRIVATE)
                    prefs.edit().putBoolean("isLoggedIn", false).apply()
                    auth.signOut()
                    findNavController().navigate(R.id.homeFragment)
                    true
                }
                else -> false
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