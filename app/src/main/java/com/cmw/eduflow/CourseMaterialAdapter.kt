package com.cmw.eduflow

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cmw.eduflow.databinding.ItemCourseMaterialDetailedBinding
import java.text.SimpleDateFormat
import java.util.*

class CourseMaterialAdapter : ListAdapter<CourseMaterial, CourseMaterialAdapter.MaterialViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {
        val binding = ItemCourseMaterialDetailedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MaterialViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MaterialViewHolder, position: Int) {
        val material = getItem(position)
        holder.bind(material)
    }

    class MaterialViewHolder(private val binding: ItemCourseMaterialDetailedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(material: CourseMaterial) {
            binding.tvMaterialTitle.text = material.title
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvUploadDate.text = sdf.format(material.uploadedAt.toDate())

            // Change icon based on a pseudo file type check
            if (material.fileUrl.contains(".pdf")) {
                binding.ivFileType.setImageResource(R.drawable.ic_file_pdf)
            } else {
                binding.ivFileType.setImageResource(R.drawable.ic_file_video)
            }

            binding.ivDownload.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(material.fileUrl))
                binding.root.context.startActivity(intent)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CourseMaterial>() {
        override fun areItemsTheSame(oldItem: CourseMaterial, newItem: CourseMaterial) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: CourseMaterial, newItem: CourseMaterial) = oldItem == newItem
    }
}