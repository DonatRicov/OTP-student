package hr.foi.air.otpstudent.domain.model

data class UserProfile(
    val fullName: String = "",
    val email: String = "",
    val major: String = "",
    val location: String = "",
    val avatarUrl: String = ""
)
