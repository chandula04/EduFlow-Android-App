package com.cmw.eduflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cmw.eduflow.databinding.FragmentAssignmentsListBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AssignmentsListFragment : Fragment() {
    private var _binding: FragmentAssignmentsListBinding? = null
    private val binding get() = _binding!!
    private lateinit var assignmentAdapter: AssignmentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAssignmentsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        // ✅ Tell adapter the user is a student
        assignmentAdapter = AssignmentAdapter(
            userRole = "student",
            onEditClick = { /* Students cannot edit */ },
            onDeleteClick = { /* Students cannot delete */ }
        )
        binding.rvAssignments.adapter = assignmentAdapter

        FirebaseFirestore.getInstance().collection("assignments")
            .orderBy("dueDate", Query.Direction.DESCENDING)
            .get().addOnSuccessListener { result ->
                assignmentAdapter.submitList(result.toObjects(Assignment::class.java))
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}