package hr.foi.air.otpstudent.ui.jobs

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
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
import com.google.firebase.auth.FirebaseAuth
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.di.AppModule
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class JobsAddFavoritesFragment : Fragment(R.layout.fragment_jobs_add_favorites) {

    private lateinit var rvJobs: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var etSearch: com.google.android.material.textfield.TextInputEditText
    private lateinit var btnFilter: MaterialButton

    private lateinit var adapter: JobAdapter

    private val viewModel: JobsAddFavoritesViewModel by lazy {
        ViewModelProvider(this, VmFactory())[JobsAddFavoritesViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        view.findViewById<ImageButton?>(R.id.btnChatbot)?.setOnClickListener {
            findNavController().navigate(R.id.chatbotFragment)
        }


        view.findViewById<TextView>(R.id.btnFavourites).setOnClickListener {
            findNavController().popBackStack()
        }

        rvJobs = view.findViewById(R.id.rvJobs)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        etSearch = view.findViewById(R.id.etSearch)
        btnFilter = view.findViewById(R.id.btnFilter)


        btnFilter.setOnClickListener {
            Toast.makeText(requireContext(), "TODO: Filter", Toast.LENGTH_SHORT).show()
        }

        adapter = JobAdapter { job ->
            viewModel.addToFavorites(job.id)
        }

        rvJobs.layoutManager = LinearLayoutManager(requireContext())
        rvJobs.adapter = adapter

        etSearch.addTextChangedListener { text ->
            viewModel.onQueryChanged(text?.toString().orEmpty())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                adapter.submitList(state.visibleJobs)

                if (state.error != null) {
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                }

                val empty = !state.isLoading && state.visibleJobs.isEmpty()
                tvEmpty.visibility = if (empty) View.VISIBLE else View.GONE
                rvJobs.visibility = if (empty) View.GONE else View.VISIBLE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.closeScreen.collectLatest {
                findNavController().popBackStack()
            }
        }

        viewModel.load()
    }

    private inner class VmFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(JobsAddFavoritesViewModel::class.java)) {
                val repo = AppModule.jobRepository
                val userIdProvider = { FirebaseAuth.getInstance().currentUser?.uid }
                @Suppress("UNCHECKED_CAST")
                return JobsAddFavoritesViewModel(repo, userIdProvider) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
