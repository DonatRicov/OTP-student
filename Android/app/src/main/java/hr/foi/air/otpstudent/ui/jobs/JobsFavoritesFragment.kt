package hr.foi.air.otpstudent.ui.jobs

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
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

class JobsFavoritesFragment : Fragment(R.layout.fragment_jobs_favorites) {

    private lateinit var adapter: JobAdapter
    private lateinit var rvJobs: RecyclerView
    private lateinit var etSearch: TextInputEditText
    private lateinit var btnFilter: MaterialButton
    private lateinit var tvActiveFilters: TextView
    private lateinit var tvEmpty: TextView

    private val viewModel: JobsFavoritesViewModel by lazy {
        ViewModelProvider(this, FavoritesVmFactory())[JobsFavoritesViewModel::class.java]
    }

    private val activeFilters = mutableSetOf<FavoritesFilter>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.btnFavourites).setOnClickListener {
            findNavController().popBackStack()
        }

        tvEmpty = view.findViewById(R.id.tvEmpty)
        rvJobs = view.findViewById(R.id.rvJobs)
        etSearch = view.findViewById(R.id.etSearch)
        btnFilter = view.findViewById(R.id.btnFilter)
        tvActiveFilters = view.findViewById(R.id.tvActiveFilters)

        adapter = JobAdapter { job ->
            val intent = Intent(requireContext(), JobDetailsActivity::class.java)
            intent.putExtra("JOB_ID", job.id)
            startActivity(intent)
        }

        rvJobs.layoutManager = LinearLayoutManager(requireContext())
        rvJobs.adapter = adapter

        etSearch.addTextChangedListener { text ->
            viewModel.onQueryChanged(text?.toString().orEmpty())
        }

        btnFilter.setOnClickListener {
            showFilterDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                render(state)
            }
        }

        viewModel.load()
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    private fun render(state: JobsFavoritesUiState) {
        adapter.submitList(state.visibleJobs)

        if (state.error != null) {
            Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
        }

        if (!state.isLoading && state.visibleJobs.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = if (state.allJobs.isEmpty()) "Nemate spremljenih favorita." else "Nema rezultata pretrage."
            rvJobs.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvJobs.visibility = View.VISIBLE
        }

        updateActiveFiltersLabel(state.activeFilters)
    }

    private fun showFilterDialog() {
        val labels = arrayOf(
            "Aktivni poslovi",
            "Moje prijave",
            "Najbolje plaćeni"
        )

        val filterMap = arrayOf(
            FavoritesFilter.ACTIVE,
            FavoritesFilter.APPLIED,
            FavoritesFilter.BEST_PAID
        )

        val checkedItems = BooleanArray(labels.size) { index ->
            activeFilters.contains(filterMap[index])
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filtriraj favorite")
            .setMultiChoiceItems(labels, checkedItems) { _, which, isChecked ->
                val filter = filterMap[which]
                if (isChecked) activeFilters.add(filter) else activeFilters.remove(filter)
            }
            .setNegativeButton("Odustani", null)
            .setPositiveButton("Primijeni") { _, _ ->
                viewModel.onFiltersChanged(activeFilters.toSet())
            }
            .setNeutralButton("Očisti") { _, _ ->
                activeFilters.clear()
                viewModel.onFiltersChanged(emptySet())
            }
            .show()
    }

    private fun updateActiveFiltersLabel(filters: Set<FavoritesFilter>) {
        if (filters.isEmpty()) {
            tvActiveFilters.visibility = View.GONE
            return
        }

        if (filters.size == FavoritesFilter.values().size) {
            tvActiveFilters.visibility = View.VISIBLE
            tvActiveFilters.text = "Svi su filteri primjenjeni"
            return
        }

        val parts = mutableListOf<String>()
        if (FavoritesFilter.ACTIVE in filters) parts.add("aktivni")
        if (FavoritesFilter.APPLIED in filters) parts.add("moje prijave")
        if (FavoritesFilter.BEST_PAID in filters) parts.add("najbolje plaćeni")

        tvActiveFilters.visibility = View.VISIBLE
        tvActiveFilters.text = "Odabrani: " + parts.joinToString(", ")
    }

    private inner class FavoritesVmFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(JobsFavoritesViewModel::class.java)) {
                val repo = AppModule.jobRepository
                val userIdProvider = { FirebaseAuth.getInstance().currentUser?.uid }
                @Suppress("UNCHECKED_CAST")
                return JobsFavoritesViewModel(repo, userIdProvider) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
