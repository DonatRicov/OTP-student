package hr.foi.air.otpstudent.ui.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.otpstudent.domain.model.Job
import hr.foi.air.otpstudent.domain.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JobsFavoritesViewModel(
    private val repo: JobRepository,
    private val userIdProvider: () -> String?
) : ViewModel() {

    private val _state = MutableStateFlow(JobsFavoritesUiState(isLoading = true))
    val state: StateFlow<JobsFavoritesUiState> = _state

    fun load() {
        val uid = userIdProvider()
        if (uid.isNullOrBlank()) {
            _state.update {
                it.copy(
                    isLoading = false,
                    allFavorites = emptyList(),
                    visibleFavorites = emptyList(),
                    recommendations = emptyList(),
                    error = "Morate biti prijavljeni."
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {

                val allJobs = repo.getJobsForUser(uid)

                val favorites = allJobs.filter { it.isFavorite }
                val nonFavorites = allJobs.filter { !it.isFavorite }

                val visibleFavs = applyFilters(
                    jobs = favorites,
                    query = _state.value.query,
                    filters = _state.value.activeFilters
                )

                val recs = pickRandomTwo(nonFavorites)

                _state.update { s ->
                    s.copy(
                        isLoading = false,
                        allFavorites = favorites,
                        visibleFavorites = visibleFavs,
                        recommendations = recs,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Greška") }
            }
        }
    }

    fun onQueryChanged(q: String) {
        _state.update { s ->
            val newState = s.copy(query = q)
            newState.copy(
                visibleFavorites = applyFilters(newState.allFavorites, newState.query, newState.activeFilters)
            )
        }
    }

    fun onFiltersChanged(filters: Set<FavoritesFilter>) {
        _state.update { s ->
            val newState = s.copy(activeFilters = filters)
            newState.copy(
                visibleFavorites = applyFilters(newState.allFavorites, newState.query, newState.activeFilters)
            )
        }
    }

    private fun pickRandomTwo(list: List<Job>): List<Job> {
        if (list.isEmpty()) return emptyList()
        val shuffled = list.shuffled()
        return if (shuffled.size >= 2) shuffled.take(2) else shuffled
    }

    private fun applyFilters(jobs: List<Job>, query: String, filters: Set<FavoritesFilter>): List<Job> {
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
                if (FavoritesFilter.ACTIVE in filters) ok = ok && !job.isClosed
                if (FavoritesFilter.APPLIED in filters) ok = ok && job.isApplied
                if (FavoritesFilter.BEST_PAID in filters) {
                    val rate = if (job.hourlyRateMax > 0.0) job.hourlyRateMax else job.hourlyRate
                    ok = ok && rate >= 8.0
                }
                ok
            }
        }

        return filtered
    }
}
