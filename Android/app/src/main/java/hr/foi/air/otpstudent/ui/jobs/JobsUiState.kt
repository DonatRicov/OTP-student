package hr.foi.air.otpstudent.ui.jobs

import hr.foi.air.otpstudent.domain.model.Job

data class JobsUiState(
    val isLoading: Boolean = false,
    val allJobs: List<Job> = emptyList(),
    val visibleJobs: List<Job> = emptyList(),
    val query: String = "",
    val activeFilters: Set<JobFilter> = emptySet(),
    val error: String? = null
)

enum class JobFilter { ACTIVE, APPLIED, FAVORITE, BEST_PAID }
