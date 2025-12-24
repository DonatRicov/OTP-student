package hr.foi.air.otpstudent.ui.auth

sealed interface PinUnlockEffect {
    data object GoToMain : PinUnlockEffect
    data object GoToLogin : PinUnlockEffect
    data class ShowMessage(val message: String) : PinUnlockEffect
}
