package hr.foi.air.otpstudent.ui.jobs

import hr.foi.air.otpstudent.domain.model.Job

data class JobDetailsUiState(
    val isLoading: Boolean = false,
    val job: Job? = null,
    val error: String? = null
)
