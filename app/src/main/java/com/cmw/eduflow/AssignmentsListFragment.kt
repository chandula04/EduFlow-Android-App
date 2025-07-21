package com.cmw.eduflow

import android.app.Activity
import android.app.AlertDialog
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
                Toast.makeText(context, "File selected. Now uploading...", Toast.LENGTH_SHORT).show()
                if (isReuploading) {
                    reuploadSubmissionToCloudinary()
                } else {
                    uploadSubmissionToCloudinary()
                }
            }
        }
    }

    private var lastClickedAssignmentId: String = ""
    private var lastClickedSubmission: Submission? = null
    private var isReuploading = false

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
                isReuploading = false
                launchFilePicker()
            },
            onSubmissionClick = { submission ->
                lastClickedSubmission = submission
                showSubmissionOptionsDialog()
            },
            onViewSubmissionsClick = { /* Students do not view submissions */ }
        )
        binding.rvAssignments.adapter = assignmentAdapter

        fetchAssignmentsAndSubmissions()
    }

    private fun launchFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/pdf" }
        filePickerLauncher.launch(intent)
    }

    private fun fetchAssignmentsAndSubmissions() {
        setLoading(true)
        val studentId = auth.currentUser?.uid ?: return

        db.collection("assignments")
            .orderBy("dueDate", Query.Direction.DESCENDING)
            .get().addOnSuccessListener { assignmentsResult ->
                val assignments = assignmentsResult.toObjects(Assignment::class.java)

                db.collection("submissions").whereEqualTo("studentId", studentId).get()
                    .addOnSuccessListener { submissionsResult ->
                        val submissions = submissionsResult.toObjects(Submission::class.java)
                        val submissionMap = submissions.associateBy { it.assignmentId }

                        val assignmentsWithSubmissions = assignments.map { assignment ->
                            Pair(assignment, submissionMap[assignment.id])
                        }

                        assignmentAdapter.submitList(assignmentsWithSubmissions)
                        setLoading(false)
                    }
            }
    }

    private fun showSubmissionOptionsDialog() {
        val options = arrayOf("Re-upload Answer", "Delete Submission")
        AlertDialog.Builder(requireContext())
            .setTitle("Submission Options")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> { // Re-upload
                        isReuploading = true
                        launchFilePicker()
                    }
                    1 -> { // Delete
                        showDeleteSubmissionConfirmation()
                    }
                }
            }
            .show()
    }

    private fun showDeleteSubmissionConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Submission")
            .setMessage("Are you sure you want to delete your submission?")
            .setPositiveButton("Delete") { _, _ ->
                lastClickedSubmission?.let {
                    db.collection("submissions").document(it.id).delete()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Submission deleted.", Toast.LENGTH_SHORT).show()
                            fetchAssignmentsAndSubmissions() // Refresh list
                        }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun uploadSubmissionToCloudinary() {
        if (selectedFileUri == null || lastClickedAssignmentId.isEmpty()) return
        setLoading(true)
        MediaManager.get().upload(selectedFileUri)
            .unsigned("eduflow_unsigned")
            .callback(object: UploadCallback {
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"].toString()
                    saveSubmissionToFirestore(url)
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

    private fun reuploadSubmissionToCloudinary() {
        if (selectedFileUri == null || lastClickedSubmission == null) return
        setLoading(true)
        MediaManager.get().upload(selectedFileUri)
            .unsigned("eduflow_unsigned")
            .callback(object: UploadCallback {
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val newUrl = resultData["secure_url"].toString()
                    db.collection("submissions").document(lastClickedSubmission!!.id)
                        .update("fileUrl", newUrl, "submittedAt", Timestamp.now())
                        .addOnSuccessListener {
                            setLoading(false)
                            Toast.makeText(context, "Answer re-uploaded successfully!", Toast.LENGTH_SHORT).show()
                            fetchAssignmentsAndSubmissions()
                        }
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    setLoading(false)
                    Toast.makeText(context, "Re-upload Error: ${error.description}", Toast.LENGTH_LONG).show()
                }
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }

    private fun saveSubmissionToFirestore(fileUrl: String) {
        val user = auth.currentUser
        if (user == null) { setLoading(false); return }

        db.collection("users").document(user.uid).get().addOnSuccessListener { userDoc ->
            val studentName = userDoc.getString("name") ?: "Unknown Student"
            val submissionId = db.collection("submissions").document().id
            val submission = Submission(
                id = submissionId,
                assignmentId = lastClickedAssignmentId,
                studentId = user.uid,
                studentName = studentName,
                fileUrl = fileUrl,
                submittedAt = Timestamp.now()
            )

            db.collection("submissions").document(submissionId).set(submission)
                .addOnSuccessListener {
                    setLoading(false)
                    Toast.makeText(context, "Answer submitted successfully!", Toast.LENGTH_SHORT).show()
                    fetchAssignmentsAndSubmissions() // Refresh the list
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