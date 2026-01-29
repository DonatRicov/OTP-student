package hr.foi.air.otpstudent.ui.internship

import android.os.Bundle
import android.view.View
import android.widget.ImageView
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
import java.util.Date

class InternshipMyApplicationsFragment : Fragment(R.layout.fragment_internship_my_applications) {

    private lateinit var rv: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: InternshipAdapter

    private val viewModel: InternshipListViewModel by lazy {
        ViewModelProvider(this, VmFactory())[InternshipListViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val header = view.findViewById<View>(R.id.includeHeader)
        header.findViewById<View>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }
        header.findViewById<View>(R.id.btnHome).setOnClickListener {
            findNavController().navigate(R.id.nav_home)
        }


        view.findViewById<View>(R.id.cardMentorship).setOnClickListener {
            findNavController().navigate(R.id.mentorshipDetailsFragment)
        }
        view.findViewById<View>(R.id.cardOtpInternship).setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.not_implemented_yet), Toast.LENGTH_SHORT).show()
        }

        rv = view.findViewById(R.id.rvInternships)
        tvEmpty = view.findViewById(R.id.tvEmpty)

        adapter = InternshipAdapter { internship ->
            findNavController().navigate(
                R.id.internshipDetailsFragment,
                bundleOf(InternshipDetailsFragment.ARG_INTERNSHIP_ID to internship.id)
            )
        }

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter


        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { s ->
                val now = Date()
                val completed = s.all.filter { it.isApplied && it.expiresAt?.toDate()?.before(now) == true }

                adapter.submitList(completed)

                val empty = !s.isLoading && completed.isEmpty()
                tvEmpty.visibility = if (empty) View.VISIBLE else View.GONE
                rv.visibility = if (empty) View.GONE else View.VISIBLE
            }
        }


        viewModel.setFilter(InternshipFilter.APPLIED, true)
        viewModel.applyFilters()
        viewModel.load()
    }

    private inner class VmFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InternshipListViewModel::class.java)) {
                val repo = AppModule.internshipRepository
                val uidProvider = { FirebaseAuth.getInstance().currentUser?.uid }
                @Suppress("UNCHECKED_CAST")
                return InternshipListViewModel(repo, uidProvider) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
