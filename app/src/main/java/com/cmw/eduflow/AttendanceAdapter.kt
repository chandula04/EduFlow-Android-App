package com.cmw.eduflow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cmw.eduflow.databinding.ItemAttendanceBinding
import java.text.SimpleDateFormat
import java.util.*

class AttendanceAdapter(
    private val records: List<AttendanceRecord>,
    private val userRole: String,
    private val onDeleteClick: (AttendanceRecord) -> Unit
) : RecyclerView.Adapter<AttendanceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAttendanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(records[position], userRole, onDeleteClick)
    }

    override fun getItemCount() = records.size

    class ViewHolder(private val binding: ItemAttendanceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(record: AttendanceRecord, userRole: String, onDeleteClick: (AttendanceRecord) -> Unit) {
            binding.tvStudentName.text = record.studentName

            val dateSdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvAttendanceDate.text = "Date: ${dateSdf.format(record.timestamp.toDate())}"

            val timeSdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            binding.tvAttendanceTime.text = "Time: ${timeSdf.format(record.timestamp.toDate())}"

            if (userRole == "teacher") {
                binding.ivDeleteAttendance.visibility = View.VISIBLE
                binding.ivDeleteAttendance.setOnClickListener { onDeleteClick(record) }
            } else {
                binding.ivDeleteAttendance.visibility = View.GONE
            }
        }
    }
}