package com.cmw.eduflow

import android.app.AlertDialog
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cmw.eduflow.databinding.FragmentSubjectsBinding
import com.google.firebase.firestore.FirebaseFirestore

class SubjectsFragment : Fragment() {
    private var _binding: FragmentSubjectsBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var subjectsAdapter: SubjectsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSubjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val animDrawable = binding.subjectsContainer.background as AnimationDrawable
        animDrawable.setEnterFadeDuration(10)
        animDrawable.setExitFadeDuration(5000)
        animDrawable.start()

        db = FirebaseFirestore.getInstance()
        setupRecyclerView()

        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        binding.fabAddSubject.setOnClickListener {
            showSubjectDialog(null)
        }

        db.collection("subjects").addSnapshotListener { snapshots, e ->
            if (e != null) { return@addSnapshotListener }
            val subjects = snapshots?.toObjects(Subject::class.java)
            subjectsAdapter.submitList(subjects)
        }
    }

    private fun setupRecyclerView() {
        subjectsAdapter = SubjectsAdapter(
            onItemClick = { subject ->
                val action = SubjectsFragmentDirections.actionSubjectsFragmentToMaterialsListFragment(subject.id, subject.name)
                findNavController().navigate(action)
            },
            onEditClick = { subject ->
                showSubjectDialog(subject)
            },
            onDeleteClick = { subject ->
                deleteSubject(subject)
            }
        )
        binding.rvSubjects.adapter = subjectsAdapter
    }

    private fun showSubjectDialog(subjectToEdit: Subject?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_subject, null)
        val etSubjectName = dialogView.findViewById<EditText>(R.id.etSubjectName)
        val isEditing = subjectToEdit != null
        if (isEditing) {
            etSubjectName.setText(subjectToEdit?.name)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (isEditing) "Edit Subject" else "Add New Subject")
            .setView(dialogView)
            .setPositiveButton(if (isEditing) "Update" else "Add") { _, _ ->
                val name = etSubjectName.text.toString().trim()
                if (name.isNotEmpty()) {
                    if (isEditing) {
                        db.collection("subjects").document(subjectToEdit!!.id).update("name", name)
                    } else {
                        val subjectId = db.collection("subjects").document().id
                        val newSubject = Subject(id = subjectId, name = name)
                        db.collection("subjects").document(subjectId).set(newSubject)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteSubject(subject: Subject) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Subject")
            .setMessage("Are you sure you want to delete '${subject.name}'? All materials inside will also be deleted.")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("subjects").document(subject.id).delete()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}