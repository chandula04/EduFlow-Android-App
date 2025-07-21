package com.cmw.eduflow

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cmw.eduflow.databinding.ItemAssignmentBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AssignmentAdapter(
    private val userRole: String,
    private val onEditClick: (Assignment) -> Unit,
    private val onDeleteClick: (Assignment) -> Unit,
    private val onUploadClick: (Assignment) -> Unit,
    private val onSubmissionClick: (Submission) -> Unit // ✅ To handle clicks on "Uploaded" button
) : ListAdapter<Pair<Assignment, Submission?>, AssignmentAdapter.AssignmentViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
        val binding = ItemAssignmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AssignmentViewHolder(binding, onEditClick, onDeleteClick, onUploadClick, onSubmissionClick)
    }

    override fun onBindViewHolder(holder: AssignmentViewHolder, position: Int) {
        holder.bind(getItem(position), userRole)
    }

    class AssignmentViewHolder(
        private val binding: ItemAssignmentBinding,
        private val onEditClick: (Assignment) -> Unit,
        private val onDeleteClick: (Assignment) -> Unit,
        private val onUploadClick: (Assignment) -> Unit,
        private val onSubmissionClick: (Submission) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(assignmentPair: Pair<Assignment, Submission?>, userRole: String) {
            val assignment = assignmentPair.first
            val submission = assignmentPair.second

            binding.tvAssignmentTitle.text = assignment.title
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvDueDate.text = "Due: ${sdf.format(assignment.dueDate.toDate())}"

            // ... (Dynamic status logic is unchanged)

            // ✅ Show/hide buttons based on role AND if a submission exists
            if (userRole == "teacher") {
                binding.btnUploadAnswer.visibility = View.GONE
                binding.btnUploaded.visibility = View.GONE
            } else { // Student
                if (submission != null) {
                    binding.btnUploadAnswer.visibility = View.GONE
                    binding.btnUploaded.visibility = View.VISIBLE
                } else {
                    binding.btnUploadAnswer.visibility = View.VISIBLE
                    binding.btnUploaded.visibility = View.GONE
                }
            }

            // Set click listeners
            binding.btnUploadAnswer.setOnClickListener { onUploadClick(assignment) }
            if (submission != null) {
                binding.btnUploaded.setOnClickListener { onSubmissionClick(submission) }
            }

            // ... (Other listeners are unchanged)
        }
    }

    // ✅ MODIFIED to handle the new data structure (Pair)
    class DiffCallback : DiffUtil.ItemCallback<Pair<Assignment, Submission?>>() {
        override fun areItemsTheSame(oldItem: Pair<Assignment, Submission?>, newItem: Pair<Assignment, Submission?>): Boolean {
            return oldItem.first.id == newItem.first.id
        }
        override fun areContentsTheSame(oldItem: Pair<Assignment, Submission?>, newItem: Pair<Assignment, Submission?>): Boolean {
            return oldItem == newItem
        }
    }
}