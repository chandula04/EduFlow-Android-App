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

class CourseMaterialsFragment : Fragment() {

    private var _binding: FragmentCourseMaterialsBinding? = null
    private val binding get() = _binding!!

    private lateinit var materialAdapter: CourseMaterialAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCourseMaterialsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        materialAdapter = CourseMaterialAdapter()
        binding.rvMaterials.adapter = materialAdapter

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        FirebaseFirestore.getInstance().collection("materials")
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val materials = result.toObjects(CourseMaterial::class.java)
                materialAdapter.submitList(materials)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}