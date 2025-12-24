package hr.foi.air.otpstudent.ui.internship

import hr.foi.air.otpstudent.domain.model.Internship

data class InternshipAppliedUiState(
    val isLoading: Boolean = false,
    val all: List<Internship> = emptyList(),
    val visible: List<Internship> = emptyList(),
    val query: String = "",
    val error: String? = null
)
