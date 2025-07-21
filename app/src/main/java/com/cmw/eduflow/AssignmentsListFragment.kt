package com.cmw.eduflow

import android.app.Activity
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
import com.cmw.eduflow.databinding.FragmentAssignmentsListBinding
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AssignmentsListFragment : Fragment() {
    private var _binding: FragmentAssignmentsListBinding? = null
    private val binding get() = _binding!!
    private lateinit var assignmentAdapter: AssignmentAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var selectedFileUri: Uri? = null

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedFileUri = uri
                // Here you would typically get the assignment object to upload for
                // For simplicity, we assume the user remembers which assignment they clicked.
                Toast.makeText(context, "File selected. Now uploading...", Toast.LENGTH_SHORT).show()
                uploadSubmissionToCloudinary(lastClickedAssignmentId, lastClickedAssignmentTitle)
            }
        }
    }

    // To keep track of which assignment the user wants to submit for
    private var lastClickedAssignmentId: String = ""
    private var lastClickedAssignmentTitle: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAssignmentsListBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        assignmentAdapter = AssignmentAdapter(
            userRole = "student",
            onEditClick = { /* Students cannot edit */ },
            onDeleteClick = { /* Students cannot delete */ },
            onUploadClick = { assignment ->
                lastClickedAssignmentId = assignment.id
                lastClickedAssignmentTitle = assignment.title
                // Launch file picker for images and PDFs
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*,application/pdf" }
                filePickerLauncher.launch(intent)
            }
        )
        binding.rvAssignments.adapter = assignmentAdapter

        fetchAssignments()
    }

    private fun fetchAssignments() {
        setLoading(true)
        db.collection("assignments")
            .orderBy("dueDate", Query.Direction.DESCENDING)
            .get().addOnSuccessListener { result ->
                assignmentAdapter.submitList(result.toObjects(Assignment::class.java))
                setLoading(false)
            }
    }

    private fun uploadSubmissionToCloudinary(assignmentId: String, assignmentTitle: String) {
        if (selectedFileUri == null) return
        setLoading(true)
        MediaManager.get().upload(selectedFileUri)
            .unsigned("eduflow_unsigned")
            .callback(object: UploadCallback {
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"].toString()
                    saveSubmissionToFirestore(assignmentId, url)
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

    private fun saveSubmissionToFirestore(assignmentId: String, fileUrl: String) {
        val user = auth.currentUser
        if (user == null) {
            setLoading(false); return
        }

        // Get student's name from their own user document
        db.collection("users").document(user.uid).get().addOnSuccessListener { userDoc ->
            val studentName = userDoc.getString("name") ?: "Unknown Student"
            val submissionId = db.collection("submissions").document().id
            val submission = Submission(
                id = submissionId,
                assignmentId = assignmentId,
                studentId = user.uid,
                studentName = studentName,
                fileUrl = fileUrl,
                submittedAt = Timestamp.now()
            )

            db.collection("submissions").document(submissionId).set(submission)
                .addOnSuccessListener {
                    setLoading(false)
                    Toast.makeText(context, "Answer submitted successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    setLoading(false)
                    Toast.makeText(context, "Failed to save submission.", Toast.LENGTH_SHORT).show()
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