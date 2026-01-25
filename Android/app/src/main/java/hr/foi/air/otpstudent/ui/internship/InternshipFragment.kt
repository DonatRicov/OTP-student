package hr.foi.air.otpstudent.ui.internship

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.di.AppModule
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InternshipFragment : Fragment(R.layout.fragment_internship) {

    private lateinit var adapter: InternshipAdapter
    private lateinit var rv: RecyclerView
    private lateinit var etSearch: TextInputEditText
    private lateinit var btnFilter: MaterialButton
    private lateinit var tvActiveFilters: TextView
    private lateinit var tvEmpty: TextView

    private lateinit var btnMyApplications: TextView

    private val viewModel: InternshipListViewModel by lazy {
        ViewModelProvider(this, VmFactory())[InternshipListViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv = view.findViewById(R.id.rvInternships)
        etSearch = view.findViewById(R.id.etSearch)
        btnFilter = view.findViewById(R.id.btnFilter)
        tvActiveFilters = view.findViewById(R.id.tvActiveFilters)
        tvEmpty = view.findViewById(R.id.tvEmpty)

        btnMyApplications = view.findViewById(R.id.btnMyApplications)
        btnMyApplications.setOnClickListener {
            val enabled = viewModel.isFilterEnabled(InternshipFilter.APPLIED)
            viewModel.setFilter(InternshipFilter.APPLIED, !enabled)
            viewModel.applyFilters()
        }

        view.findViewById<ImageButton>(R.id.btnChatbot).setOnClickListener {
            findNavController().navigate(R.id.chatbotFragment)
        }

        // ✅ OVDJE JE KLIK IZ LISTE PRAKSI (promjena: Activity -> Fragment navigation)
        adapter = InternshipAdapter { internship ->
            findNavController().navigate(
                R.id.internshipDetailsFragment,
                bundleOf(InternshipDetailsFragment.ARG_INTERNSHIP_ID to internship.id)
            )
        }

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        etSearch.addTextChangedListener { text ->
            viewModel.onQueryChanged(text?.toString().orEmpty())
        }

        btnFilter.setOnClickListener { showFilterDialog() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { s ->
                adapter.submitList(s.visible)

                if (!s.isLoading && s.visible.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    rv.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    rv.visibility = View.VISIBLE
                }

                if (s.error != null) {
                    Toast.makeText(requireContext(), s.error, Toast.LENGTH_LONG).show()
                }

                tvActiveFilters.visibility = if (s.activeFiltersText.isBlank()) View.GONE else View.VISIBLE
                tvActiveFilters.text = s.activeFiltersText
            }
        }

        viewModel.load()
    }

    private fun showFilterDialog() {
        val labels = arrayOf("Aktivne", "Moje prijave", "Favoriti", "Najbolje plaćene")
        val map = arrayOf(
            InternshipFilter.ACTIVE,
            InternshipFilter.APPLIED,
            InternshipFilter.FAVORITE,
            InternshipFilter.BEST_PAID
        )

        val checked = BooleanArray(labels.size) { idx ->
            viewModel.isFilterEnabled(map[idx])
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filtriraj prakse")
            .setMultiChoiceItems(labels, checked) { _, which, isChecked ->
                viewModel.setFilter(map[which], isChecked)
            }
            .setNegativeButton("Odustani", null)
            .setPositiveButton("Primijeni") { _, _ -> viewModel.applyFilters() }
            .setNeutralButton("Očisti") { _, _ -> viewModel.clearFilters() }
            .show()
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
