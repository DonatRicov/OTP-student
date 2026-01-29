package hr.foi.air.otpstudent.ui.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.otpstudent.domain.model.Job
import hr.foi.air.otpstudent.domain.repository.JobRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JobsAddFavoritesViewModel(
    private val repo: JobRepository,
    private val userIdProvider: () -> String?
) : ViewModel() {

    private val _state = MutableStateFlow(JobsAddFavoritesUiState(isLoading = true))
    val state: StateFlow<JobsAddFavoritesUiState> = _state


    val closeScreen = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun load() {
        val uid = userIdProvider()
        if (uid.isNullOrBlank()) {
            _state.update { it.copy(isLoading = false, error = "Morate biti prijavljeni.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {

                val all = repo.getJobsForUser(uid)
                val nonFav = all.filter { !it.isFavorite }

                _state.update { s ->
                    val newState = s.copy(isLoading = false, allJobs = nonFav)
                    newState.copy(visibleJobs = applyQuery(newState.allJobs, newState.query))
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Greška") }
            }
        }
    }

    fun onQueryChanged(q: String) {
        _state.update { s ->
            val ns = s.copy(query = q)
            ns.copy(visibleJobs = applyQuery(ns.allJobs, ns.query))
        }
    }

    fun addToFavorites(jobId: String) {
        val uid = userIdProvider() ?: return

        viewModelScope.launch {
            try {
                repo.setFavorite(uid, jobId, true)


                _state.update { s ->
                    val updatedAll = s.allJobs.filterNot { it.id == jobId }
                    val updatedVisible = applyQuery(updatedAll, s.query)
                    s.copy(allJobs = updatedAll, visibleJobs = updatedVisible)
                }


                closeScreen.tryEmit(Unit)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Greška") }
            }
        }
    }

    private fun applyQuery(list: List<Job>, query: String): List<Job> {
        val lower = query.lowercase().trim()
        if (lower.isEmpty()) return list

        return list.filter { job ->
            job.title.lowercase().contains(lower) ||
                    job.location.lowercase().contains(lower) ||
                    job.company.lowercase().contains(lower)
        }
    }
}
