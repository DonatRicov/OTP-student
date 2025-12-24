package hr.foi.air.otpstudent.ui.cv

sealed interface CvEffect {
    data class ShowMessage(val message: String) : CvEffect
}
