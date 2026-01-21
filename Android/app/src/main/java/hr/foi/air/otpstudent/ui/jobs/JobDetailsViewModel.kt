package hr.foi.air.otpstudent.ui.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.otpstudent.domain.model.Job
import hr.foi.air.otpstudent.domain.repository.JobRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JobDetailsViewModel(
    private val repo: JobRepository,
    private val userIdProvider: () -> String?
) : ViewModel() {

    private val _state = MutableStateFlow(JobDetailsUiState(isLoading = true))
    val state: StateFlow<JobDetailsUiState> = _state

    private val _effects = Channel<JobDetailsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var currentJobId: String? = null

    fun load(jobId: String) {
        currentJobId = jobId
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val uid = userIdProvider()

            // 🔒 VIEW EVENT NE SMIJE BLOKIRATI UI
            if (uid != null) {
                try {
                    repo.markViewed(uid, jobId)
                } catch (e: Exception) {
                    // samo log, NIKAD crash
                    e.printStackTrace()
                }
            }

            try {
                val job = repo.getJobDetailsForUser(uid, jobId)
                if (job == null) {
                    _effects.trySend(JobDetailsEffect.ShowMessage("Posao nije pronađen."))
                    _effects.trySend(JobDetailsEffect.Close)
                    return@launch
                }
                _state.update { it.copy(isLoading = false, job = job) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }



    fun toggleFavorite() {
        val uid = userIdProvider() ?: run {
            _effects.trySend(JobDetailsEffect.ShowMessage("Morate biti prijavljeni."))
            return
        }
        val job = _state.value.job ?: return
        val newFav = !job.isFavorite

        viewModelScope.launch {
            try {
                repo.setFavorite(uid, job.id, newFav)
                _state.update { it.copy(job = job.copy(isFavorite = newFav)) }
            } catch (e: Exception) {
                _effects.trySend(JobDetailsEffect.ShowMessage("Greška pri spremanju favorita."))
            }
        }
    }

    fun onApplyClicked() {
        val uid = userIdProvider() ?: run {
            _effects.trySend(JobDetailsEffect.ShowMessage("Morate biti prijavljeni."))
            return
        }

        val job = _state.value.job ?: return
        val url = job.applyUrl
        if (url.isBlank()) {
            _effects.trySend(JobDetailsEffect.ShowMessage("Link za prijavu nije dostupan."))
            return
        }

        viewModelScope.launch {
            try {
                repo.markApplied(uid, job.id)
                _state.update { it.copy(job = job.copy(isApplied = true)) }
                _effects.trySend(JobDetailsEffect.OpenUrl(url))
            } catch (e: Exception) {
                _effects.trySend(JobDetailsEffect.ShowMessage("Greška pri spremanju prijave."))
            }
        }
    }
}
