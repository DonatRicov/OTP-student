package hr.foi.air.otpstudent.ui.internship

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.di.AppModule
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InternshipFragment : Fragment(R.layout.fragment_internship) {

    private lateinit var rvInternships: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: InternshipAdapter

    private val viewModel: InternshipAppliedViewModel by lazy {
        ViewModelProvider(this, VmFactory())[InternshipAppliedViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Header
        val headerContainer = view.findViewById<FrameLayout>(R.id.headerContainer)
        headerContainer.removeAllViews()
        val headerView = layoutInflater.inflate(R.layout.view_header, headerContainer, false)
        headerContainer.addView(headerView)

        headerView.findViewById<View>(R.id.btnChatbot)?.setOnClickListener {
            findNavController().navigate(R.id.chatbotFragment)
        }

        // Kartice
        view.findViewById<View>(R.id.cardMentorship).setOnClickListener {
            findNavController().navigate(R.id.mentorshipDetailsFragment)
        }

        view.findViewById<View>(R.id.cardOtpInternship).setOnClickListener {
            val hasListDestination =
                runCatching { findNavController().graph.findNode(R.id.internshipListFragment) }.getOrNull() != null

            if (hasListDestination) {
                findNavController().navigate(R.id.internshipListFragment)
            } else {
                Toast.makeText(requireContext(), getString(R.string.not_implemented_yet), Toast.LENGTH_SHORT).show()
            }
        }

        // Lista
        rvInternships = view.findViewById(R.id.rvInternships)
        tvEmpty = view.findViewById(R.id.tvEmpty)

        adapter = InternshipAdapter { internship ->
            findNavController().navigate(
                R.id.internshipDetailsFragment,
                bundleOf(InternshipDetailsFragment.ARG_INTERNSHIP_ID to internship.id)
            )
        }

        rvInternships.layoutManager = LinearLayoutManager(requireContext())
        rvInternships.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { s ->
                adapter.submitList(s.visible)

                val empty = !s.isLoading && s.visible.isEmpty()
                tvEmpty.visibility = if (empty) View.VISIBLE else View.GONE
                rvInternships.visibility = if (empty) View.GONE else View.VISIBLE

                if (s.error != null) {
                    Toast.makeText(requireContext(), s.error, Toast.LENGTH_LONG).show()
                }
            }
        }

        // Load data
        viewModel.load()
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    private inner class VmFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InternshipAppliedViewModel::class.java)) {
                val repo = AppModule.internshipRepository
                val uidProvider = { FirebaseAuth.getInstance().currentUser?.uid }
                @Suppress("UNCHECKED_CAST")
                return InternshipAppliedViewModel(repo, uidProvider) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
