package com.cmw.eduflow

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
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
    private val onEditClick: (Assignment) -> Unit,
    private val onDeleteClick: (Assignment) -> Unit
) : ListAdapter<Assignment, AssignmentAdapter.AssignmentViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
        val binding = ItemAssignmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AssignmentViewHolder(binding, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: AssignmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AssignmentViewHolder(
        private val binding: ItemAssignmentBinding,
        private val onEditClick: (Assignment) -> Unit,
        private val onDeleteClick: (Assignment) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(assignment: Assignment) {
            binding.tvAssignmentTitle.text = assignment.title

            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvDueDate.text = "Due: ${sdf.format(assignment.dueDate.toDate())}"

            // ✅ DYNAMIC STATUS LOGIC
            var statusText = assignment.status
            var statusColor = Color.parseColor("#FFB800") // Default to Pending color
            var statusBackground = R.drawable.chip_background_pending

            val now = Calendar.getInstance().time
            if (assignment.status.equals("Graded", ignoreCase = true)) {
                statusText = "Graded"
                statusColor = Color.parseColor("#28A745") // Green
                statusBackground = R.drawable.chip_background_graded
            } else if (assignment.dueDate.toDate().before(now)) {
                // It's past the due date
                val diffInMillis = now.time - assignment.dueDate.toDate().time
                val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

                if (diffInDays > 20) {
                    statusText = "Overdue"
                    statusColor = Color.parseColor("#DC3545") // Red
                    statusBackground = R.drawable.chip_background_overdue
                } else if (diffInDays in 0..4) { // 0 days means it was due today but is now past time
                    statusText = "Late"
                    statusColor = Color.parseColor("#FD7E14") // Orange
                    statusBackground = R.drawable.chip_background_late
                } else {
                    // This handles cases where it's 5-20 days late
                    statusText = "Overdue"
                    statusColor = Color.parseColor("#DC3545") // Red
                    statusBackground = R.drawable.chip_background_overdue
                }
            }

            binding.tvStatus.text = statusText
            binding.tvStatus.setTextColor(statusColor)
            binding.tvStatus.background = ContextCompat.getDrawable(binding.root.context, statusBackground)

            binding.ivEdit.setOnClickListener { onEditClick(assignment) }
            binding.ivDelete.setOnClickListener { onDeleteClick(assignment) }

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

    class DiffCallback : DiffUtil.ItemCallback<Assignment>() {
        override fun areItemsTheSame(oldItem: Assignment, newItem: Assignment): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Assignment, newItem: Assignment): Boolean {
            return oldItem == newItem
        }
    }
}