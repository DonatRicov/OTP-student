package hr.foi.air.otpstudent.ui.internship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.otpstudent.domain.model.Internship
import hr.foi.air.otpstudent.domain.repository.InternshipRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InternshipAppliedViewModel(
    private val repo: InternshipRepository,
    private val userIdProvider: () -> String?
) : ViewModel() {

    private val _state = MutableStateFlow(InternshipAppliedUiState(isLoading = true))
    val state: StateFlow<InternshipAppliedUiState> = _state

    fun load() {
        val uid = userIdProvider() ?: run {
            _state.update { it.copy(isLoading = false, all = emptyList(), visible = emptyList(), error = "Morate biti prijavljeni.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val list = repo.getAppliedInternshipsForUser(uid)
                _state.update { s ->
                    val ns = s.copy(isLoading = false, all = list)
                    ns.copy(visible = applySearch(ns.all, ns.query))
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Greška") }
            }
        }
    }

    fun onQueryChanged(q: String) {
        _state.update { s ->
            val ns = s.copy(query = q)
            ns.copy(visible = applySearch(ns.all, ns.query))
        }
    }

    private fun applySearch(list: List<Internship>, q: String): List<Internship> {
        val lower = q.lowercase().trim()
        if (lower.isEmpty()) return list
        return list.filter {
            it.title.lowercase().contains(lower) ||
                    it.location.lowercase().contains(lower) ||
                    it.company.lowercase().contains(lower)
        }
    }
}
