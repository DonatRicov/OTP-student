package hr.foi.air.otpstudent.ui.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.otpstudent.domain.model.Job
import hr.foi.air.otpstudent.domain.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JobsViewModel(
    private val repo: JobRepository,
    private val userIdProvider: () -> String?
) : ViewModel() {

    private val _state = MutableStateFlow(JobsUiState(isLoading = true))
    val state: StateFlow<JobsUiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val jobs = repo.getJobsForUser(userIdProvider())
                _state.update {
                    val s = it.copy(isLoading = false, allJobs = jobs)
                    s.copy(visibleJobs = applyFilters(s.allJobs, s.query, s.activeFilters))
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Greška") }
            }
        }
    }

    fun onQueryChanged(q: String) {
        _state.update { s ->
            val newState = s.copy(query = q)
            newState.copy(visibleJobs = applyFilters(newState.allJobs, newState.query, newState.activeFilters))
        }
    }

    fun onFiltersChanged(filters: Set<JobFilter>) {
        _state.update { s ->
            val newState = s.copy(activeFilters = filters)
            newState.copy(visibleJobs = applyFilters(newState.allJobs, newState.query, newState.activeFilters))
        }
    }

    private fun applyFilters(jobs: List<Job>, query: String, filters: Set<JobFilter>): List<Job> {
        var filtered = jobs
        val lower = query.lowercase().trim()

        if (lower.isNotEmpty()) {
            filtered = filtered.filter { job ->
                job.title.lowercase().contains(lower) ||
                        job.location.lowercase().contains(lower) ||
                        job.company.lowercase().contains(lower)
            }
        }

        if (filters.isNotEmpty()) {
            filtered = filtered.filter { job ->
                var ok = true
                if (JobFilter.ACTIVE in filters) ok = ok && !job.isClosed
                if (JobFilter.APPLIED in filters) ok = ok && job.isApplied
                if (JobFilter.FAVORITE in filters) ok = ok && job.isFavorite
                if (JobFilter.BEST_PAID in filters) {
                    val rate = if (job.hourlyRateMax > 0.0) job.hourlyRateMax else job.hourlyRate
                    ok = ok && rate >= 8.0
                }
                ok
            }
        }

        return filtered
    }
}
