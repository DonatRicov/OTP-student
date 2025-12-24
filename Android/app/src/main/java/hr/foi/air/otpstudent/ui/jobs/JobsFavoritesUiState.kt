package hr.foi.air.otpstudent.ui.jobs

import hr.foi.air.otpstudent.domain.model.Job

data class JobsFavoritesUiState(
    val isLoading: Boolean = false,
    val allJobs: List<Job> = emptyList(),
    val visibleJobs: List<Job> = emptyList(),
    val query: String = "",
    val activeFilters: Set<FavoritesFilter> = emptySet(),
    val error: String? = null
)

enum class FavoritesFilter { ACTIVE, APPLIED, BEST_PAID }
