package hr.foi.air.otpstudent.ui.internship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.otpstudent.domain.repository.InternshipRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InternshipDetailsViewModel(
    private val repo: InternshipRepository,
    private val userIdProvider: () -> String?
) : ViewModel() {

    private val _state = MutableStateFlow(InternshipDetailsUiState())
    val state: StateFlow<InternshipDetailsUiState> = _state

    private var internshipId: String? = null

    fun load(id: String) {
        internshipId = id
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val uid = userIdProvider()

            if (uid != null) {
                try {
                    repo.markViewed(uid, id)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            try {
                val internship = repo.getInternshipById(id)
                if (internship == null) {
                    _state.update { it.copy(isLoading = false, error = "Internship nije pronađen.") }
                    return@launch
                }

                val fav = if (!uid.isNullOrBlank()) repo.isFavorite(uid, id) else false
                val applied = if (!uid.isNullOrBlank()) repo.isApplied(uid, id) else false

                _state.update {
                    it.copy(
                        isLoading = false,
                        internship = internship,
                        isFavorite = fav,
                        isApplied = applied
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Greška") }
            }
        }
    }


    fun toggleFavorite() {
        val id = internshipId ?: return
        val uid = userIdProvider() ?: run {
            _state.update { it.copy(error = "Morate biti prijavljeni.") }
            return
        }

        viewModelScope.launch {
            try {
                val newValue = !_state.value.isFavorite
                repo.setFavorite(uid, id, newValue)
                _state.update { it.copy(isFavorite = newValue) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Greška pri spremanju favorita.") }
            }
        }
    }

    fun apply() {
        val id = internshipId ?: return
        val uid = userIdProvider() ?: run {
            _state.update { it.copy(error = "Morate biti prijavljeni.") }
            return
        }

        if (_state.value.isApplied) return

        viewModelScope.launch {
            try {
                repo.markApplied(uid, id)
                _state.update { it.copy(isApplied = true) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Greška pri prijavi.") }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
