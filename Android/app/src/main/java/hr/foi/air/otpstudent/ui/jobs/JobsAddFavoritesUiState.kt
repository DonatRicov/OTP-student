package hr.foi.air.otpstudent.ui.jobs

import hr.foi.air.otpstudent.domain.model.Job

data class JobsAddFavoritesUiState(
    val isLoading: Boolean = false,
    val allJobs: List<Job> = emptyList(),
    val visibleJobs: List<Job> = emptyList(),
    val query: String = "",
    val error: String? = null
)
