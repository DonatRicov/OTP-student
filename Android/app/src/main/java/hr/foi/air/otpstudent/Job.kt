package hr.foi.air.otpstudent

import com.google.firebase.Timestamp

data class Job(
    val id: String = "",
    val title: String = "",
    val company: String = "",
    val location: String = "",
    val hourlyRate: Double = 0.0,
    val hourlyRateMax: Double = 0.0,
    val applicantsCount: Int = 0,
    val postedAt: Timestamp? = null,
    val expiresAt: Timestamp? = null,
    val isClosed: Boolean = false,
    val isApplied: Boolean = false,
    val isFavorite: Boolean = false,
    val description: String = ""
)
