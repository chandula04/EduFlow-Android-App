package com.cmw.eduflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.cmw.eduflow.databinding.FragmentSubmissionsListBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SubmissionsListFragment : Fragment() {
    private var _binding: FragmentSubmissionsListBinding? = null
    private val binding get() = _binding!!
    private val args: SubmissionsListFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSubmissionsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.rvSubmissions.layoutManager = LinearLayoutManager(context)

        // Fetch submissions for the specific assignment
        FirebaseFirestore.getInstance().collection("submissions")
            .whereEqualTo("assignmentId", args.assignmentId)
            .orderBy("submittedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val submissions = result.toObjects(Submission::class.java)
                binding.rvSubmissions.adapter = SubmissionsAdapter(submissions)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}