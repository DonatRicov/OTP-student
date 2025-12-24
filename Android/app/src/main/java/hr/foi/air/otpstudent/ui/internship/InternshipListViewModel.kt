package hr.foi.air.otpstudent.ui.internship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.otpstudent.domain.model.Internship
import hr.foi.air.otpstudent.domain.repository.InternshipRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InternshipListViewModel(
    private val repo: InternshipRepository,
    private val userIdProvider: () -> String?
) : ViewModel() {

    private val _state = MutableStateFlow(InternshipListUiState(isLoading = true))
    val state: StateFlow<InternshipListUiState> = _state

    fun load() {
        val uid = userIdProvider() // može biti null (guest)
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val list = repo.getInternshipsForUser(uid)
                _state.update { s ->
                    val ns = s.copy(isLoading = false, all = list)
                    ns.copy(
                        visible = applyAll(ns.all, ns.query, ns.filters),
                        activeFiltersText = filtersLabel(ns.filters)
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Greška pri dohvaćanju praksi") }
            }
        }
    }

    fun onQueryChanged(q: String) {
        _state.update { s ->
            val ns = s.copy(query = q)
            ns.copy(visible = applyAll(ns.all, ns.query, ns.filters))
        }
    }

    fun isFilterEnabled(f: InternshipFilter) = _state.value.filters.contains(f)

    fun setFilter(f: InternshipFilter, enabled: Boolean) {
        _state.update { s ->
            val newSet = s.filters.toMutableSet()
            if (enabled) newSet.add(f) else newSet.remove(f)
            s.copy(filters = newSet)
        }
    }

    fun applyFilters() {
        _state.update { s ->
            s.copy(
                visible = applyAll(s.all, s.query, s.filters),
                activeFiltersText = filtersLabel(s.filters)
            )
        }
    }

    fun clearFilters() {
        _state.update { s ->
            s.copy(
                filters = emptySet(),
                visible = applyAll(s.all, s.query, emptySet()),
                activeFiltersText = ""
            )
        }
    }

    private fun applyAll(list: List<Internship>, q: String, filters: Set<InternshipFilter>): List<Internship> {
        var out = list

        val lower = q.lowercase().trim()
        if (lower.isNotEmpty()) {
            out = out.filter {
                it.title.lowercase().contains(lower) ||
                        it.location.lowercase().contains(lower) ||
                        it.company.lowercase().contains(lower)
            }
        }

        if (filters.isNotEmpty()) {
            out = out.filter { internship ->
                var ok = true
                if (InternshipFilter.ACTIVE in filters) ok = ok && !internship.isClosed
                if (InternshipFilter.APPLIED in filters) ok = ok && internship.isApplied
                if (InternshipFilter.FAVORITE in filters) ok = ok && internship.isFavorite
                if (InternshipFilter.BEST_PAID in filters) {
                    val rate = if (internship.hourlyRateMax > 0.0) internship.hourlyRateMax else internship.hourlyRate
                    ok = ok && rate >= 8.0
                }
                ok
            }
        }

        return out
    }

    private fun filtersLabel(filters: Set<InternshipFilter>): String {
        if (filters.isEmpty()) return ""
        val parts = mutableListOf<String>()
        if (InternshipFilter.ACTIVE in filters) parts.add("aktivne")
        if (InternshipFilter.APPLIED in filters) parts.add("moje prijave")
        if (InternshipFilter.FAVORITE in filters) parts.add("favoriti")
        if (InternshipFilter.BEST_PAID in filters) parts.add("najbolje plaćene")
        return "Odabrani: " + parts.joinToString(", ")
    }
}
