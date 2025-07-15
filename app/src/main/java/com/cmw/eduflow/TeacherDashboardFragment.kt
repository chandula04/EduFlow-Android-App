package com.cmw.eduflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cmw.eduflow.databinding.FragmentTeacherDashboardBinding

class TeacherDashboardFragment : Fragment() {

    private var _binding: FragmentTeacherDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Remove old animation and data fetching logic for now

        binding.btnScanQr.setOnClickListener {
            // TODO: Add QR Scanner Logic
            Toast.makeText(context, "QR Scanner Clicked!", Toast.LENGTH_SHORT).show()
        }

        // You can add other click listeners for "Create Now", "Upload Now", etc.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}