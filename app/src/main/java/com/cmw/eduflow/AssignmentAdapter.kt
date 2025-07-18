package com.cmw.eduflow

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
import android.graphics.Color

class AssignmentAdapter : ListAdapter<Assignment, AssignmentAdapter.AssignmentViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
        val binding = ItemAssignmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AssignmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AssignmentViewHolder, position: Int) {
        val assignment = getItem(position)
        holder.bind(assignment)
    }

    class AssignmentViewHolder(private val binding: ItemAssignmentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(assignment: Assignment) {
            binding.tvAssignmentTitle.text = assignment.title

            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvDueDate.text = "Due: ${sdf.format(assignment.dueDate.toDate())}"

            binding.tvStatus.text = assignment.status

            // Change status chip color based on status
            when (assignment.status.lowercase()) {
                "pending" -> {
                    binding.tvStatus.background = ContextCompat.getDrawable(binding.root.context, R.drawable.chip_background_pending)
                    binding.tvStatus.setTextColor(Color.parseColor("#FFB800"))
                }
                "graded" -> {
                    binding.tvStatus.background = ContextCompat.getDrawable(binding.root.context, R.drawable.chip_background_graded)
                    binding.tvStatus.setTextColor(Color.parseColor("#28A745"))
                }
                "overdue" -> {
                    binding.tvStatus.background = ContextCompat.getDrawable(binding.root.context, R.drawable.chip_background_overdue)
                    binding.tvStatus.setTextColor(Color.parseColor("#DC3545"))
                }
            }

            binding.ivEdit.setOnClickListener {
                Toast.makeText(binding.root.context, "Edit clicked for ${assignment.title}", Toast.LENGTH_SHORT).show()
            }
            binding.ivView.setOnClickListener {
                Toast.makeText(binding.root.context, "View clicked for ${assignment.title}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Assignment>() {
        override fun areItemsTheSame(oldItem: Assignment, newItem: Assignment) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Assignment, newItem: Assignment) = oldItem == newItem
    }
}