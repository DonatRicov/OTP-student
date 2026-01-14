package hr.foi.air.otpstudent.domain.model

import com.google.firebase.Timestamp

data class ChallengeState(
    val challengeId: String,
    val status: String,
    val completedAt: Timestamp? = null,
    val claimDeadlineAt: Timestamp? = null,
    val claimedAt: Timestamp? = null
)
