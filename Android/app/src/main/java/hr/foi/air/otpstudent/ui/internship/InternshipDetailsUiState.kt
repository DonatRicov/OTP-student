package hr.foi.air.otpstudent.ui.internship

import hr.foi.air.otpstudent.domain.model.Internship

data class InternshipDetailsUiState(
    val isLoading: Boolean = true,
    val internship: Internship? = null,
    val isFavorite: Boolean = false,
    val isApplied: Boolean = false,
    val error: String? = null
)
