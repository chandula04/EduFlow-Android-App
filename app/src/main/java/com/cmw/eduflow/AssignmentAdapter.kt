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
    private val onSubmissionClick: (Submission) -> Unit,
    private val onViewSubmissionsClick: (Assignment) -> Unit
) : ListAdapter<Pair<Assignment, Submission?>, AssignmentAdapter.AssignmentViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
        val binding = ItemAssignmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AssignmentViewHolder(binding, onEditClick, onDeleteClick, onUploadClick, onSubmissionClick, onViewSubmissionsClick)
    }

    override fun onBindViewHolder(holder: AssignmentViewHolder, position: Int) {
        holder.bind(getItem(position), userRole)
    }

    class AssignmentViewHolder(
        private val binding: ItemAssignmentBinding,
        private val onEditClick: (Assignment) -> Unit,
        private val onDeleteClick: (Assignment) -> Unit,
        private val onUploadClick: (Assignment) -> Unit,
        private val onSubmissionClick: (Submission) -> Unit,
        private val onViewSubmissionsClick: (Assignment) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(assignmentPair: Pair<Assignment, Submission?>, userRole: String) {
            val assignment = assignmentPair.first
            val submission = assignmentPair.second

            binding.tvAssignmentTitle.text = assignment.title
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvDueDate.text = "Due: ${sdf.format(assignment.dueDate.toDate())}"

            // Dynamic status logic
            var statusText = assignment.status
            var statusColor = Color.parseColor("#FFB800") // Default to Pending color
            var statusBackground = R.drawable.chip_background_pending

            val now = Calendar.getInstance().time
            if (assignment.status.equals("Graded", ignoreCase = true)) {
                statusText = "Graded"
                statusColor = Color.parseColor("#28A745") // Green
                statusBackground = R.drawable.chip_background_graded
            } else if (assignment.dueDate.toDate().before(now)) {
                val diffInMillis = now.time - assignment.dueDate.toDate().time
                val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

                if (diffInDays in 0..4) {
                    statusText = "Late"
                    statusColor = Color.parseColor("#FD7E14") // Orange
                    statusBackground = R.drawable.chip_background_late
                } else {
                    statusText = "Overdue"
                    statusColor = Color.parseColor("#DC3545") // Red
                    statusBackground = R.drawable.chip_background_overdue
                }
            }

            binding.tvStatus.text = statusText
            binding.tvStatus.setTextColor(statusColor)
            binding.tvStatus.background = ContextCompat.getDrawable(binding.root.context, statusBackground)

            // Show/hide buttons based on role
            if (userRole == "teacher") {
                binding.btnUploadAnswer.visibility = View.GONE
                binding.btnUploaded.visibility = View.GONE
                binding.ivEdit.visibility = View.VISIBLE
                binding.ivDelete.visibility = View.VISIBLE
                binding.btnViewSubmissions.visibility = View.VISIBLE
            } else { // Student
                binding.ivEdit.visibility = View.GONE
                binding.ivDelete.visibility = View.GONE
                binding.btnViewSubmissions.visibility = View.GONE
                if (submission != null) {
                    binding.btnUploadAnswer.visibility = View.GONE
                    binding.btnUploaded.visibility = View.VISIBLE
                } else {
                    binding.btnUploadAnswer.visibility = View.VISIBLE
                    binding.btnUploaded.visibility = View.GONE
                }
            }

            // Set click listeners
            binding.ivEdit.setOnClickListener { onEditClick(assignment) }
            binding.ivDelete.setOnClickListener { onDeleteClick(assignment) }
            binding.btnUploadAnswer.setOnClickListener { onUploadClick(assignment) }
            binding.btnViewSubmissions.setOnClickListener { onViewSubmissionsClick(assignment) }
            if (submission != null) {
                binding.btnUploaded.setOnClickListener { onSubmissionClick(submission) }
            }

            binding.ivDownload.setOnClickListener {
                if (assignment.fileUrl.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(assignment.fileUrl))
                    binding.root.context.startActivity(intent)
                } else {
                    Toast.makeText(binding.root.context, "No file available for download.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Pair<Assignment, Submission?>>() {
        override fun areItemsTheSame(oldItem: Pair<Assignment, Submission?>, newItem: Pair<Assignment, Submission?>): Boolean {
            return oldItem.first.id == newItem.first.id
        }
        override fun areContentsTheSame(oldItem: Pair<Assignment, Submission?>, newItem: Pair<Assignment, Submission?>): Boolean {
            return oldItem == newItem
        }
    }
}