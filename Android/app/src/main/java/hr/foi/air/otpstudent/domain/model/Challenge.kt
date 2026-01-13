package hr.foi.air.otpstudent.domain.model

data class Challenge(
    val id: String,
    val title: String,
    val rewardPoints: Long,
    val claimWindowDay: Long,
    val active: Boolean,
    val type: String
)
