package hr.foi.air.otpstudent.ui.jobs

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
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
import android.widget.ImageButton
import androidx.navigation.fragment.findNavController
class JobsFragment : Fragment(R.layout.fragment_jobs) {

    private lateinit var adapter: JobAdapter
    private lateinit var rvJobs: RecyclerView
    private lateinit var etSearch: TextInputEditText
    private lateinit var tvMyApplications: TextView
    private lateinit var btnFilter: MaterialButton
    private lateinit var tvActiveFilters: TextView
    private lateinit var tvEmpty: TextView

    private val viewModel: JobsViewModel by lazy {
        ViewModelProvider(
            this,
            JobsViewModelFactory()
        )[JobsViewModel::class.java]
    }

    private val activeFilters = mutableSetOf<JobFilter>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvEmpty = view.findViewById(R.id.tvEmpty)
        rvJobs = view.findViewById(R.id.rvJobs)
        etSearch = view.findViewById(R.id.etSearch)
        tvMyApplications = view.findViewById(R.id.btnFavourites)
        btnFilter = view.findViewById(R.id.btnFilter)
        tvActiveFilters = view.findViewById(R.id.tvActiveFilters)

        view.findViewById<ImageButton>(R.id.btnChatbot).setOnClickListener {
            findNavController().navigate(R.id.chatbotFragment)
        }
        tvMyApplications.setOnClickListener {
            findNavController().navigate(R.id.nav_jobs_favorites)
        }

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

    private fun render(state: JobsUiState) {
        adapter.submitList(state.visibleJobs)

        if (!state.isLoading && state.visibleJobs.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
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
            "Favoriti",
            "Najbolje plaćeni"
        )

        val filterMap = arrayOf(
            JobFilter.ACTIVE,
            JobFilter.APPLIED,
            JobFilter.FAVORITE,
            JobFilter.BEST_PAID
        )

        val checkedItems = BooleanArray(labels.size) { index ->
            activeFilters.contains(filterMap[index])
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filtriraj poslove")
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

    private fun updateActiveFiltersLabel(filters: Set<JobFilter>) {
        if (filters.isEmpty()) {
            tvActiveFilters.visibility = View.GONE
            return
        }

        if (filters.size == JobFilter.values().size) {
            tvActiveFilters.visibility = View.VISIBLE
            tvActiveFilters.text = "Svi su filteri primjenjeni"
            return
        }

        val parts = mutableListOf<String>()
        if (JobFilter.ACTIVE in filters) parts.add("aktivni")
        if (JobFilter.APPLIED in filters) parts.add("moje prijave")
        if (JobFilter.FAVORITE in filters) parts.add("favoriti")
        if (JobFilter.BEST_PAID in filters) parts.add("najbolje plaćeni")

        tvActiveFilters.visibility = View.VISIBLE
        tvActiveFilters.text = "Odabrani: " + parts.joinToString(", ")
    }

    private inner class JobsViewModelFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(JobsViewModel::class.java)) {
                val repo = AppModule.jobRepository
                val userIdProvider = { FirebaseAuth.getInstance().currentUser?.uid }
                @Suppress("UNCHECKED_CAST")
                return JobsViewModel(repo, userIdProvider) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
