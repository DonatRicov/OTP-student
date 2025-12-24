package hr.foi.air.otpstudent.ui.auth

sealed interface LoginEffect {
    data object GoToSuccess : LoginEffect
    data class ShowMessage(val message: String) : LoginEffect
}
