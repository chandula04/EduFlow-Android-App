package com.cmw.eduflow

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cmw.eduflow.databinding.ItemSubmissionBinding
import java.text.SimpleDateFormat
import java.util.*

class SubmissionsAdapter(private var submissions: List<Submission>) : RecyclerView.Adapter<SubmissionsAdapter.SubmissionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionViewHolder {
        val binding = ItemSubmissionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubmissionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubmissionViewHolder, position: Int) {
        holder.bind(submissions[position])
    }

    override fun getItemCount() = submissions.size

    class SubmissionViewHolder(private val binding: ItemSubmissionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(submission: Submission) {
            binding.tvStudentName.text = submission.studentName
            val sdf = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
            binding.tvSubmissionDate.text = "Submitted: ${sdf.format(submission.submittedAt.toDate())}"
            binding.btnDownloadSubmission.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(submission.fileUrl))
                binding.root.context.startActivity(intent)
            }
        }
    }
}