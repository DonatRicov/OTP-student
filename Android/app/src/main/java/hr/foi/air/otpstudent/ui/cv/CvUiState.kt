package hr.foi.air.otpstudent.ui.cv

import hr.foi.air.otpstudent.domain.model.CvDocument

data class CvUiState(
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val cvs: List<CvDocument> = emptyList(),
    val error: String? = null
)
