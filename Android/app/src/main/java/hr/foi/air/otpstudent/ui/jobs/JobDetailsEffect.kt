package hr.foi.air.otpstudent.ui.jobs

sealed interface JobDetailsEffect {
    data class OpenUrl(val url: String) : JobDetailsEffect
    data class ShowMessage(val message: String) : JobDetailsEffect
    data object Close : JobDetailsEffect
}
