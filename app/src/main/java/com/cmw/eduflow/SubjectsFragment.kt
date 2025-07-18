package com.cmw.eduflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cmw.eduflow.databinding.FragmentSubjectsBinding
import com.google.firebase.firestore.FirebaseFirestore

class SubjectsFragment : Fragment() {
    private var _binding: FragmentSubjectsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSubjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        // In a real app, you'd fetch this from Firestore.
        // For now, we'll create a static list.
        val subjects = listOf(
            Subject("sci", "Science"),
            Subject("math", "Maths"),
            Subject("hist", "History"),
            Subject("sin", "Sinhala")
        )

        binding.rvSubjects.adapter = SubjectsAdapter(subjects) { selectedSubject ->
            val action = SubjectsFragmentDirections.actionSubjectsFragmentToMaterialsListFragment(
                subjectId = selectedSubject.id,
                subjectName = selectedSubject.name
            )
            findNavController().navigate(action)
        }
    }
}