package com.cmw.eduflow

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cmw.eduflow.databinding.ItemCourseMaterialDetailedBinding
import java.text.SimpleDateFormat
import java.util.*

class CourseMaterialAdapter(
    private val userRole: String,
    private val onEditClick: (CourseMaterial) -> Unit,
    private val onDeleteClick: (CourseMaterial) -> Unit
) : ListAdapter<CourseMaterial, CourseMaterialAdapter.MaterialViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {
        val binding = ItemCourseMaterialDetailedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MaterialViewHolder(binding, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: MaterialViewHolder, position: Int) {
        holder.bind(getItem(position), userRole)
    }

    class MaterialViewHolder(
        private val binding: ItemCourseMaterialDetailedBinding,
        private val onEditClick: (CourseMaterial) -> Unit,
        private val onDeleteClick: (CourseMaterial) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(material: CourseMaterial, userRole: String) {
            binding.tvMaterialTitle.text = material.lessonTitle
            binding.tvSubjectName.text = "Subject: ${material.subjectName}"

            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvUploadDate.text = "Uploaded: ${sdf.format(material.uploadedAt.toDate())}"

            if (material.fileType == "video") {
                binding.ivFileType.setImageResource(R.drawable.ic_file_video)
            } else {
                binding.ivFileType.setImageResource(R.drawable.ic_file_pdf)
            }

            // Show/hide buttons based on the user's role
            if (userRole == "teacher") {
                binding.ivEdit.visibility = View.VISIBLE
                binding.ivDelete.visibility = View.VISIBLE
            } else { // "student"
                binding.ivEdit.visibility = View.GONE
                binding.ivDelete.visibility = View.GONE
            }

            binding.ivDownload.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(material.fileUrl))
                binding.root.context.startActivity(intent)
            }

            binding.ivEdit.setOnClickListener { onEditClick(material) }
            binding.ivDelete.setOnClickListener { onDeleteClick(material) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CourseMaterial>() {
        override fun areItemsTheSame(oldItem: CourseMaterial, newItem: CourseMaterial): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: CourseMaterial, newItem: CourseMaterial): Boolean {
            return oldItem == newItem
        }
    }
}