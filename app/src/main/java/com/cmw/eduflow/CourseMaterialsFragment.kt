package com.cmw.eduflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cmw.eduflow.databinding.FragmentCourseMaterialsBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CourseMaterialFragment : Fragment() {

    private var _binding: FragmentCourseMaterialsBinding? = null
    private val binding get() = _binding!!

    private lateinit var materialAdapter: CourseMaterialAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCourseMaterialsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // Fetch and display the materials from Firestore
        FirebaseFirestore.getInstance().collection("materials")
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val materials = result.toObjects(CourseMaterial::class.java)
                materialAdapter.submitList(materials)
            }
    }

    private fun setupRecyclerView() {
        // ✅ FIX: Provide empty actions for edit and delete, as students do not use them.
        materialAdapter = CourseMaterialAdapter(
            onEditClick = {
                // Students can't edit, so this does nothing.
            },
            onDeleteClick = {
                // Students can't delete, so this does nothing.
            }
        )
        binding.rvMaterials.adapter = materialAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}