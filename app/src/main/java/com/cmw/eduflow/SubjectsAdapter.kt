package com.cmw.eduflow

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cmw.eduflow.databinding.ItemSubjectBinding

class SubjectsAdapter(
    private val subjects: List<Subject>,
    private val onItemClick: (Subject) -> Unit
) : RecyclerView.Adapter<SubjectsAdapter.SubjectViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val binding = ItemSubjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.bind(subjects[position])
    }

    override fun getItemCount() = subjects.size

    inner class SubjectViewHolder(private val binding: ItemSubjectBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(subject: Subject) {
            binding.tvSubjectName.text = subject.name
            binding.root.setOnClickListener { onItemClick(subject) }
        }
    }
}