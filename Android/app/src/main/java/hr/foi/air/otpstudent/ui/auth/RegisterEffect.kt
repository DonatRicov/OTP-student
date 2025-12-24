package hr.foi.air.otpstudent.ui.auth

sealed interface RegisterEffect {
    data object GoToProfilePersonal : RegisterEffect
    data class ShowMessage(val message: String) : RegisterEffect
}
