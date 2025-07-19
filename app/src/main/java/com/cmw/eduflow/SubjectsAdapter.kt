package com.cmw.eduflow

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cmw.eduflow.databinding.ItemSubjectBinding

class SubjectsAdapter(
    private val onItemClick: (Subject) -> Unit,
    private val onEditClick: (Subject) -> Unit,
    private val onDeleteClick: (Subject) -> Unit
) : ListAdapter<Subject, SubjectsAdapter.SubjectViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val binding = ItemSubjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SubjectViewHolder(private val binding: ItemSubjectBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(subject: Subject) {
            binding.tvSubjectName.text = subject.name
            binding.subjectItemContainer.setOnClickListener { onItemClick(subject) }
            binding.ivEdit.setOnClickListener { onEditClick(subject) }
            binding.ivDelete.setOnClickListener { onDeleteClick(subject) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Subject>() {
        override fun areItemsTheSame(oldItem: Subject, newItem: Subject) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Subject, newItem: Subject) = oldItem == newItem
    }
}